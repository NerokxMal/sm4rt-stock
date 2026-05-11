package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.model.Movimiento;
import com.malcom.sm4rtstock.model.Producto;
import com.malcom.sm4rtstock.model.TipoMovimiento;
import com.malcom.sm4rtstock.repository.ProductoRepository;
import com.malcom.sm4rtstock.util.PdfExportUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ProductoRepository productoRepository;
    private final MovimientoService movimientoService;

    public byte[] exportarProductos(String format) {
        List<Producto> productos = productoRepository.findAll();
        return switch (format) {
            case "csv" -> exportarProductosCsv(productos);
            case "pdf" -> exportarProductosPdf(productos);
            default -> throw new IllegalArgumentException("Formato no soportado: " + format);
        };
    }

    public byte[] exportarMovimientos(String format, LocalDate desde, LocalDate hasta, TipoMovimiento tipo) {
        List<Movimiento> movimientos = movimientoService.obtenerHistorial(desde, hasta, tipo);
        return switch (format) {
            case "csv" -> exportarMovimientosCsv(movimientos);
            case "pdf" -> exportarMovimientosPdf(movimientos);
            default -> throw new IllegalArgumentException("Formato no soportado: " + format);
        };
    }

    private byte[] exportarProductosCsv(List<Producto> productos) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,nombre,descripcion,precio,stock,umbralCritico,categoria\n");
        for (Producto p : productos) {
            csv.append(p.getId()).append(',')
                    .append(escapeCsv(p.getNombre())).append(',')
                    .append(escapeCsv(p.getDescripcion())).append(',')
                    .append(p.getPrecio()).append(',')
                    .append(p.getStock()).append(',')
                    .append(p.getUmbralCritico()).append(',')
                    .append(escapeCsv(p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría"))
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] exportarMovimientosCsv(List<Movimiento> movimientos) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,producto,usuario,tipo,cantidad,stockAnterior,stockNuevo,motivo,fecha\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Movimiento m : movimientos) {
            csv.append(m.getId()).append(',')
                    .append(escapeCsv(m.getProducto() != null ? m.getProducto().getNombre() : "N/A")).append(',')
                    .append(escapeCsv(m.getUsuario() != null ? m.getUsuario().getUsername() : "sistema")).append(',')
                    .append(m.getTipo()).append(',')
                    .append(m.getCantidad()).append(',')
                    .append(m.getStockAnterior()).append(',')
                    .append(m.getStockNuevo()).append(',')
                    .append(escapeCsv(m.getMotivo())).append(',')
                    .append(m.getFecha() != null ? m.getFecha().format(formatter) : "")
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] exportarProductosPdf(List<Producto> productos) {
        List<String> headers = List.of("ID", "Nombre", "Stock", "Umbral", "Precio", "Categoria");
        List<List<String>> rows = productos.stream()
                .map(p -> List.of(
                        String.valueOf(p.getId()),
                        safe(p.getNombre()),
                        String.valueOf(p.getStock()),
                        String.valueOf(p.getUmbralCritico()),
                        String.valueOf(p.getPrecio()),
                        p.getCategoria() != null ? safe(p.getCategoria().getNombre()) : "Sin categoria"
                ))
                .toList();
        return PdfExportUtil.generarTabla("Reporte de Productos", headers, rows);
    }

    private byte[] exportarMovimientosPdf(List<Movimiento> movimientos) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<String> headers = List.of("Fecha", "Producto", "Tipo", "Cantidad", "Usuario", "Motivo");
        List<List<String>> rows = movimientos.stream()
                .map(m -> List.of(
                        m.getFecha() != null ? m.getFecha().format(formatter) : "",
                        m.getProducto() != null ? safe(m.getProducto().getNombre()) : "N/A",
                        m.getTipo() != null ? m.getTipo().name() : "",
                        String.valueOf(m.getCantidad()),
                        m.getUsuario() != null ? safe(m.getUsuario().getUsername()) : "sistema",
                        safe(m.getMotivo())
                ))
                .toList();
        return PdfExportUtil.generarTabla("Reporte de Movimientos", headers, rows);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
