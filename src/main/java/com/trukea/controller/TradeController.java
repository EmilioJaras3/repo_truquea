package com.trukea.controller;

import io.javalin.http.Context;
import com.trukea.ApiResponse;
import com.trukea.models.Trade;
import com.trukea.repository.TradeRepository;
import java.util.List;
import java.util.Map;

public class TradeController {
    private TradeRepository tradeRepository;

    public TradeController() {
        this.tradeRepository = new TradeRepository();
    }

    public void proposeTrade(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Trade trade = new Trade();
            trade.setProductoOfrecidoId(((Number) body.get("producto_ofrecido_id")).intValue());
            trade.setProductoDeseadoId(((Number) body.get("producto_deseado_id")).intValue());
            trade.setUsuarioOferenteId(((Number) body.get("usuario_oferente_id")).intValue());
            trade.setComentario((String) body.get("comentario"));

            if (trade.getProductoOfrecidoId() <= 0 || trade.getProductoDeseadoId() <= 0 || trade.getUsuarioOferenteId() <= 0) {
                ctx.status(400).json(new ApiResponse(false, "IDs de productos y usuario son obligatorios", null));
                return;
            }

            int usuarioReceptorId = tradeRepository.getOwnerIdByProductId(trade.getProductoDeseadoId());
            if(usuarioReceptorId == -1) {
                ctx.status(400).json(new ApiResponse(false, "Producto deseado no encontrado", null));
                return;
            }
            trade.setUsuarioReceptorId(usuarioReceptorId);

            if (trade.getUsuarioOferenteId() == usuarioReceptorId) {
                ctx.status(400).json(new ApiResponse(false, "No puedes intercambiar con tus propios productos", null));
                return;
            }

            int tradeId = tradeRepository.save(trade);
            if (tradeId > 0) {
                ctx.status(201).json(new ApiResponse(true, "Propuesta de trueque enviada exitosamente", Map.of("tradeId", tradeId)));
            } else {
                ctx.status(500).json(new ApiResponse(false, "Error al crear la propuesta", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(new ApiResponse(false, "Error interno al crear propuesta", null));
        }
    }

    public void getReceivedRequests(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<Trade> requests = tradeRepository.findReceivedByUserId(userId);
            ctx.json(new ApiResponse(true, "Solicitudes recibidas obtenidas", Map.of("requests", requests)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void getSentRequests(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<Trade> requests = tradeRepository.findSentByUserId(userId);
            ctx.json(new ApiResponse(true, "Solicitudes enviadas obtenidas", Map.of("requests", requests)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }

    public void acceptTrade(Context ctx) {
        updateTradeStatus(ctx, 2, "Trueque aceptado exitosamente");
    }

    public void rejectTrade(Context ctx) {
        updateTradeStatus(ctx, 3, "Trueque rechazado exitosamente");
    }

    public void completeTrade(Context ctx) {
        updateTradeStatus(ctx, 4, "Trueque completado exitosamente");
    }

    public void cancelTrade(Context ctx) {
        updateTradeStatus(ctx, 5, "Trueque cancelado exitosamente");
    }

    private void updateTradeStatus(Context ctx, int statusId, String successMessage) {
        try {
            int tradeId = Integer.parseInt(ctx.pathParam("id"));
            boolean success = tradeRepository.updateStatus(tradeId, statusId);
            if (success) {
                ctx.json(new ApiResponse(true, successMessage, null));
            } else {
                ctx.status(404).json(new ApiResponse(false, "Trueque no encontrado", null));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error al actualizar estado del trueque", null));
            e.printStackTrace();
        }
    }

    public void getTradeHistory(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<Trade> history = tradeRepository.findHistoryByUserId(userId);
            ctx.json(new ApiResponse(true, "Historial obtenido", Map.of("history", history)));
        } catch (Exception e) {
            ctx.status(500).json(new ApiResponse(false, "Error del servidor", null));
            e.printStackTrace();
        }
    }
}