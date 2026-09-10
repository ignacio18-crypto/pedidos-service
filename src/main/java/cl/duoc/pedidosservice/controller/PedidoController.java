package cl.duoc.pedidosservice.controller;

import cl.duoc.pedidosservice.model.EstadoPedido;
import cl.duoc.pedidosservice.model.Pedido;
import cl.duoc.pedidosservice.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@Valid @RequestBody Pedido nuevo) {
        return pedidoService.crear(nuevo);
    }

    @PutMapping("/{id}/estado")
    public Pedido actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(body.get("estado"));
        return pedidoService.actualizarEstado(id, nuevoEstado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
    }
}