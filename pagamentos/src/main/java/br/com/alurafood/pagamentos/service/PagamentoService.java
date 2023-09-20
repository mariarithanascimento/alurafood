package br.com.alurafood.pagamentos.service;

import java.util.Optional;

import br.com.alurafood.pagamentos.DTO.PagamentoDTO;
import br.com.alurafood.pagamentos.http.PedidoClient;
import br.com.alurafood.pagamentos.model.Pagamento;
import br.com.alurafood.pagamentos.model.Status;
import br.com.alurafood.pagamentos.repository.PagamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PagamentoService {
    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private PedidoClient pedido;
    @Autowired
    private ModelMapper modelMapper;

    /*
    Como podemos ter muitos pagamentos, qual vai ser a nossa estratégia? Já vamos criar um método público que vai
    retornar já paginado os objetos do tipo PagamentoDto.
    Vamos importar as dependências, e vai pegar do repositório e mandar localizar todos, "pega todos os pagamentos
    e devolve para mim e depois mapeia novamente para DTO". Visto que o repositório só entende "pagamento" e não
    "pagamentoDto", logo pedimos para ele buscar tudo e ele vai devolver um objeto do tipo pagamento e precisamos
     transformar isso em pagamento DTO.
     */
    public Page<PagamentoDTO> obterTodos(Pageable paginacao) {
        return repository
                .findAll(paginacao)
                .map(p -> modelMapper.map(p, PagamentoDTO.class));
    }

    /*
    Esse método obterPorId também vai receber um ID, localizar no repositório através do findById(), se ele não
    encontrar vai dar uma exceção de EntityNotFoundException() e se encontrar vai fazer o retorno de pagamento
    para pagamentoDto.
     */
    public PagamentoDTO obterPorId(Long id) {
        Pagamento pagamento = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException());

        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    /*
    Nesse código vamos criar um pagamento, então vai chegar uma estrutura "JSON PagamentoDto", vai ser lido esse
    pagamento, nesse momento já está na classe, já foi serializado, e vai ser transformado de DTO para pagamento,
    o status vai ser colocado como criado e será salvo no banco de dados.
     */
    public PagamentoDTO criarPagamento(PagamentoDTO dto) {
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        repository.save(pagamento);

        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    /*
    O atualizarPagamento possui o mesmo conceito, só que vamos precisar de dois parâmetros, o ID e o objeto
    pagamentoDto com os dados que queremos alterar e depois de mapear isso para pagamento, que é a classe que
    o repository entende, vamos setar o ID, ajustar e fazer a alteração no banco de dados, devolvendo novamente
    ele como pagamentoDto.
     */
    public PagamentoDTO atualizarPagamento(Long id, PagamentoDTO dto) {
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setId(id);
        pagamento = repository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    /*
    O fato de estarmos usando o JPA repository já temos essa facilidade. Então observe que "save", "delete",
    "find", "findById" é muito simples quando usamos dessa maneira para quem trabalhou com JPDC e tinha que
    escrever muito código fica mais fácil fazer dessa maneira.
    O excluir vai ser void visto que não vamos devolver nada, vamos simplesmente receber um ID e apagar.
    Então, no PagamentoService já estamos com as operações necessárias para realizarmos o nosso CRUD, o nosso
    microsserviço já está criando forma e quase chegando ao fim.
     */
    public void excluirPagamento(Long id) {

        repository.deleteById(id);
    }

    /*
   Vamos ter o método chamado confirmarPagamento que quando chamarmos passando o ID do pagamento,
   recuperamos o pagamento no banco de dados, setamos o status como confirmado, salvamos o pagamento e,
   em seguida, chamamos o client de pedido para fazer a atualização passando o ID do pedido.

   Visto que passamos o ID do pedido e quem tem essa informação é o pagamento.get()getPedidoId(). Esse getter
    é que possui a informação do pedido.
   */
    public void confirmarPagamento(Long id){
        Optional<Pagamento> pagamento = repository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        repository.save(pagamento.get());
        pedido.atualizaPagamento(pagamento.get().getPedidoId());
    }
}
