package com.trukea.service;

import com.trukea.dto.TradeDTO;
import com.trukea.dto.ProposeTradeRequestDTO;
import com.trukea.models.Trade;
import com.trukea.repository.TradeRepository;
import java.util.List;
import java.util.stream.Collectors;

public class TradeService {
    private TradeRepository tradeRepository;

    public TradeService() {
        this.tradeRepository = new TradeRepository();
    }

    public int proposeTrade(ProposeTradeRequestDTO request) {
        // Obtener el ID del propietario del producto deseado
        int usuarioReceptorId = tradeRepository.getOwnerIdByProductId(request.getProducto_deseado_id());
        if (usuarioReceptorId == -1) {
            return -1; // Producto no encontrado
        }

        Trade trade = new Trade();
        trade.setProductoOfrecidoId(request.getProducto_ofrecido_id());
        trade.setProductoDeseadoId(request.getProducto_deseado_id());
        trade.setUsuarioOferenteId(request.getUsuario_oferente_id());
        trade.setUsuarioReceptorId(usuarioReceptorId);
        trade.setComentario(request.getComentario());

        return tradeRepository.save(trade);
    }

    public List<TradeDTO> getReceivedRequests(int userId) {
        return tradeRepository.findReceivedByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TradeDTO> getSentRequests(int userId) {
        return tradeRepository.findSentByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean acceptTrade(int tradeId) {
        return tradeRepository.updateStatus(tradeId, 2); // 2 = Aceptado
    }

    public boolean rejectTrade(int tradeId) {
        return tradeRepository.updateStatus(tradeId, 3); // 3 = Rechazado
    }

    public boolean completeTrade(int tradeId) {
        return tradeRepository.updateStatus(tradeId, 4); // 4 = Completado
    }

    public boolean cancelTrade(int tradeId) {
        return tradeRepository.updateStatus(tradeId, 5); // 5 = Cancelado
    }

    private TradeDTO convertToDTO(Trade trade) {
        TradeDTO dto = new TradeDTO();
        dto.setId(trade.getId());
        dto.setProductoOfrecidoNombre(trade.getProductoOfrecidoNombre());
        dto.setProductoOfrecidoImagen(trade.getProductoOfrecidoImagen());
        dto.setProductoDeseadoNombre(trade.getProductoDeseadoNombre());
        dto.setProductoDeseadoImagen(trade.getProductoDeseadoImagen());
        dto.setUsuarioNombre(trade.getOferenteNombre());
        dto.setUsuarioApellido(trade.getOferenteApellido());
        dto.setEstadoNombre(trade.getEstadoNombre());
        dto.setComentario(trade.getComentario());
        dto.setFechaPropuesta(trade.getFechaPropuesta());
        return dto;
    }
}