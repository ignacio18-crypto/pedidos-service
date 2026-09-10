package cl.duoc.pedidosservice.service;

public class PedidoNoEncontradoException extends RuntimeException {
    public PedidoNoEncontradoException(Long id) {
        super("No existe un pedido con id " + id);
    }
}