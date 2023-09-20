package br.com.alurafood.pagamentos.controller;

import br.com.alurafood.pagamentos.DTO.PagamentoDTO;
import br.com.alurafood.pagamentos.service.PagamentoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService service;

    /*
    Para obter todos os pagamentos, como estamos fazendo uma API Rest e ela vai usar os métodos HTTP para isso,
    vamos usar a anotação @GetMapping.
    Quando esse método for feito e realizarmos uma requisição do tipo get para "/pagamentos", sem passar nada
    adicional ele vai ser chamado em PagamentoController.
    O método que vamos usar vai ser um public Page já paginado, como fizemos em serviço. Ele vai devolver com
    paginação, com uma linha conseguimos solicitar para o controlador devolver o que precisamos.
     */

    @GetMapping
    public Page<PagamentoDTO> listar(@PageableDefault(size = 10) Pageable paginacao) {
        return service.obterTodos(paginacao);
    }

    /*
    Agora o obter por ID que também vai ser um @GetMapping passando o ID do que queremos buscar. Vai ser
    "/pagamentos/id", sendo "/id" o ID do pagamento.
    Para isso, passamos o ID no caminho do end point, do endereço usando a anotação @PathVariable e passar
    que ele não pode ser nulo com o @NotNull, tem que ter sempre um ID.
    O service vai devolver um DTO com o objeto para nós. Os dois gets já programamos, um para obter todos
    e o outro para obter pagamento por ID.
     */

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDTO> detalhar(@PathVariable @NotNull Long id) {
        PagamentoDTO dto = service.obterPorId(id);

        return ResponseEntity.ok(dto);
    }

    /*
    A parte de criação é simples também, chama a service criar pagamento e, com o endereço, conseguimos
    expandir por ID o pagamento que vai ser enviado. Para devolver na resposta do pagamento, o pagamento foi
    criado para que o usuário consiga ver.
     */
    @PostMapping
    public ResponseEntity<PagamentoDTO> cadastrar(@RequestBody @Valid PagamentoDTO dto, UriComponentsBuilder uriBuilder) {
        PagamentoDTO pagamento = service.criarPagamento(dto);
        URI endereco = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();

        return ResponseEntity.created(endereco).body(pagamento);
    }

    /*
    Agora incluímos o de alterar que vai ser o put, por ser uma alteração. Temos o @PutMapping() que vai
    devolver uma ResponseEntity de pagamento DTO e ao mandar atualizar perceba que temos dois parâmetros.
    Um parâmetro vai entrar no end point que é o ID do que queremos atualizar e o outro vai vir no corpo da
    requisição com os dados para atualizar. Por isso temos a anotação de @PathVariable, sendo o ID que vai no
    caminho da rota e o @RequestBody que é o pagamento DTO, o JSON que vai vir no corpo da requisição.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDTO> atualizar(@PathVariable @NotNull Long id, @RequestBody @Valid PagamentoDTO dto) {
        PagamentoDTO atualizado = service.atualizarPagamento(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    /*
    Por fim, o delete para excluir o registro do pagamento. O @DeleteMapping("/{id}") que vai chamar o
    excluir pagamento e devolver sem conteúdo, ou seja, deu certo mas não há o que retornar, foi simplesmente
    excluído.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<PagamentoDTO> remover(@PathVariable @NotNull Long id) {
        service.excluirPagamento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
    public void confirmarPagamento(@PathVariable @NotNull Long id){

        service.confirmarPagamento(id);
    }
}
