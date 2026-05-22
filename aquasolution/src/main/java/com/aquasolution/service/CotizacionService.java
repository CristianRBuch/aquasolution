package com.aquasolution.service;

import com.aquasolution.model.Cotizacion;
import com.aquasolution.model.DetalleCotizacion;
import com.aquasolution.model.Usuario;
import com.aquasolution.repository.CotizacionRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;

    public CotizacionService(CotizacionRepository cotizacionRepository) {
        this.cotizacionRepository = cotizacionRepository;
    }

    public Cotizacion guardar(Cotizacion cotizacion) {
        if (cotizacion.getNumeroCotizacion() == null) {
            LocalDateTime now = LocalDateTime.now();
            String numero = "AS" + String.format("%02d", now.getDayOfMonth())
                    + String.format("%02d", now.getMonthValue())
                    + now.getYear();
            // Verificar si ya existe ese número y agregar sufijo único
            String numeroFinal = numero;
            int intentos = 0;
            while (cotizacionRepository.existsByNumeroCotizacion(numeroFinal)) {
                intentos++;
                numeroFinal = numero + "-" + intentos;
            }
            cotizacion.setNumeroCotizacion(numeroFinal);
        }

        BigDecimal total = BigDecimal.ZERO;
        if (cotizacion.getDetalles() != null) {
            for (DetalleCotizacion detalle : cotizacion.getDetalles()) {
                detalle.setCotizacion(cotizacion);
                // Si el subtotal ya viene calculado desde el controller lo respetamos
                // Si no, calculamos sin descuento
                if (detalle.getSubtotal() == null) {
                    BigDecimal desc = detalle.getDescuento() != null
                            ? detalle.getDescuento() : BigDecimal.ZERO;
                    BigDecimal sub = detalle.getPrecioUnitario()
                            .multiply(new BigDecimal(detalle.getCantidad()))
                            .multiply(BigDecimal.ONE.subtract(
                                    desc.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)))
                            .setScale(2, RoundingMode.HALF_UP);
                    detalle.setSubtotal(sub);
                }
                total = total.add(detalle.getSubtotal());
            }
        }
        cotizacion.setSubtotal(total);
        cotizacion.setTotal(total);
        return cotizacionRepository.save(cotizacion);
    }

    public List<Cotizacion> obtenerTodas() {
        return cotizacionRepository.findAll();
    }

    public Optional<Cotizacion> obtenerPorId(Long id) {
        return cotizacionRepository.findById(id);
    }

    public List<Cotizacion> obtenerPorCliente(Usuario cliente) {
        return cotizacionRepository.findByCliente(cliente);
    }

    public void eliminar(Long id) {
        cotizacionRepository.deleteById(id);
    }

    public Cotizacion cambiarEstado(Long id, Cotizacion.EstadoCotizacion estado) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));
        cotizacion.setEstado(estado);
        return cotizacionRepository.save(cotizacion);
    }

    public byte[] generarPDF(Long id) throws Exception {
        Cotizacion c = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 80, 50);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        BaseColor azul = new BaseColor(30, 77, 140);
        BaseColor azulOscuro = new BaseColor(26, 61, 110);
        BaseColor grisClaro = new BaseColor(240, 244, 248);

        Font fNormal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
        Font fNegrita = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.DARK_GRAY);
        Font fBlanco = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        Font fTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
        Font fSubtitulo = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE);
        Font fTotal = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, azul);
        Font fPie = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
        Font fCondicion = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.DARK_GRAY);

        // HEADER
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3, 2});

        PdfPCell cEmpresa = new PdfPCell();
        cEmpresa.setBackgroundColor(azul);
        cEmpresa.setPadding(15);
        cEmpresa.setBorder(Rectangle.NO_BORDER);
        Paragraph pEmpresa = new Paragraph("AQUA SOLUTION\n", fTitulo);
        pEmpresa.add(new Phrase("Tu mejor opción en agua", fSubtitulo));
        cEmpresa.addElement(pEmpresa);
        header.addCell(cEmpresa);

        PdfPCell cCot = new PdfPCell();
        cCot.setBackgroundColor(azulOscuro);
        cCot.setPadding(15);
        cCot.setBorder(Rectangle.NO_BORDER);
        cCot.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Font fCotLabel = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        Font fCotNum = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(173, 216, 230));
        Paragraph pCot = new Paragraph("COTIZACIÓN:\n", fCotLabel);
        pCot.add(new Phrase(c.getNumeroCotizacion(), fCotNum));
        cCot.addElement(pCot);
        header.addCell(cCot);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy",
                new java.util.Locale("es", "GT"));
        Paragraph fecha = new Paragraph("Guatemala, " + c.getFechaCreacion().format(fmt) + ".", fNormal);
        fecha.setAlignment(Element.ALIGN_RIGHT);
        doc.add(fecha);
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Apreciables señores:", fNormal));
        doc.add(new Paragraph(c.getCliente().getNombreCompleto().toUpperCase(), fNegrita));
        doc.add(new Paragraph("Presente.", fNormal));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Deseamos tenga éxitos en sus actividades. Agradecemos la oportunidad de poder brindarle la siguiente propuesta, gustosamente le dejamos las opciones para que pueda evaluar y que se ajusten a sus necesidades:", fNormal));
        doc.add(Chunk.NEWLINE);

        PdfPTable propuesta = new PdfPTable(1);
        propuesta.setWidthPercentage(100);
        PdfPCell cPropuesta = new PdfPCell(new Phrase("PROPUESTA:", fBlanco));
        cPropuesta.setBackgroundColor(azul);
        cPropuesta.setPadding(8);
        cPropuesta.setBorder(Rectangle.BOX);
        cPropuesta.setHorizontalAlignment(Element.ALIGN_CENTER);
        propuesta.addCell(cPropuesta);
        doc.add(propuesta);

        // Tabla detalles — ahora con columna de descuento
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4.5f, 1.2f, 1.2f, 1.2f, 2f});

        String[] headers2 = {"Descripción", "Unidad", "Cantidad", "Desc. %", "Precio Total"};
        for (String h : headers2) {
            PdfPCell hc = new PdfPCell(new Phrase(h, fBlanco));
            hc.setBackgroundColor(azul);
            hc.setPadding(7);
            hc.setBorder(Rectangle.BOX);
            hc.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(hc);
        }

        boolean alt = false;
        if (c.getDetalles() != null) {
            for (DetalleCotizacion d : c.getDetalles()) {
                BaseColor bg = alt ? grisClaro : BaseColor.WHITE;
                PdfPCell cd1 = new PdfPCell(new Phrase(d.getDescripcion(), fNormal));
                PdfPCell cd2 = new PdfPCell(new Phrase(d.getUnidad() != null ? d.getUnidad() : "UND", fNormal));
                PdfPCell cd3 = new PdfPCell(new Phrase(String.valueOf(d.getCantidad()), fNormal));
                String descStr = (d.getDescuento() != null && d.getDescuento().compareTo(BigDecimal.ZERO) > 0)
                        ? d.getDescuento() + "%" : "-";
                PdfPCell cd4 = new PdfPCell(new Phrase(descStr, fNormal));
                PdfPCell cd5 = new PdfPCell(new Phrase("Q." + d.getSubtotal(), fNormal));
                for (PdfPCell cel : new PdfPCell[]{cd1, cd2, cd3, cd4, cd5}) {
                    cel.setBackgroundColor(bg);
                    cel.setPadding(7);
                    cel.setBorder(Rectangle.BOX);
                }
                cd2.setHorizontalAlignment(Element.ALIGN_CENTER);
                cd3.setHorizontalAlignment(Element.ALIGN_CENTER);
                cd4.setHorizontalAlignment(Element.ALIGN_CENTER);
                cd5.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabla.addCell(cd1);
                tabla.addCell(cd2);
                tabla.addCell(cd3);
                tabla.addCell(cd4);
                tabla.addCell(cd5);
                alt = !alt;
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                PdfPCell ec = new PdfPCell(new Phrase(" "));
                ec.setPadding(7);
                ec.setBorder(Rectangle.BOX);
                tabla.addCell(ec);
            }
        }

        PdfPCell cSubLabel = new PdfPCell(new Phrase("Subtotal:", fNegrita));
        cSubLabel.setColspan(4);
        cSubLabel.setBorder(Rectangle.BOX);
        cSubLabel.setPadding(7);
        cSubLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(cSubLabel);
        PdfPCell cSubVal = new PdfPCell(new Phrase("Q." + c.getSubtotal(), fNormal));
        cSubVal.setBorder(Rectangle.BOX);
        cSubVal.setPadding(7);
        cSubVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(cSubVal);
        doc.add(tabla);

        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        PdfPCell cTotalLabel = new PdfPCell(new Phrase("TOTAL DE COTIZACIÓN", fBlanco));
        cTotalLabel.setBackgroundColor(azul);
        cTotalLabel.setBorder(Rectangle.BOX);
        cTotalLabel.setPadding(8);
        cTotalLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalTable.addCell(cTotalLabel);
        PdfPCell cTotalVal = new PdfPCell(new Phrase("Q. " + c.getTotal(), fTotal));
        cTotalVal.setBorder(Rectangle.BOX);
        cTotalVal.setPadding(8);
        cTotalVal.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalTable.addCell(cTotalVal);
        doc.add(totalTable);

        PdfPTable letrasTable = new PdfPTable(1);
        letrasTable.setWidthPercentage(100);
        PdfPCell cLetras = new PdfPCell(new Phrase("EN LETRAS:  " + convertirNumeroALetras(c.getTotal()), fNegrita));
        cLetras.setBorder(Rectangle.BOX);
        cLetras.setPadding(8);
        letrasTable.addCell(cLetras);
        doc.add(letrasTable);
        doc.add(Chunk.NEWLINE);

        String validez = c.getValidezOferta() != null ? c.getValidezOferta() : "3 Días hábiles";
        String entrega = c.getTiempoEntrega() != null ? c.getTiempoEntrega() : "5 Días hábiles";
        String pagos = c.getCondicionesPago() != null ? c.getCondicionesPago() : "Contado";
        String garantiaStr = c.getGarantia() != null ? c.getGarantia() : "Sobre desperfectos de fábrica";
        String nota = c.getNotaPrecios() != null ? c.getNotaPrecios() : "Presupuesto sujeto a cambio de precios o existencias";

        doc.add(new Paragraph("● Validez de oferta: " + validez, fCondicion));
        doc.add(new Paragraph("● Tiempo de entrega: " + entrega, fCondicion));
        doc.add(new Paragraph("● Condiciones de pago: " + pagos, fCondicion));
        doc.add(new Paragraph("● Oferta: Sujeta a negociación", fCondicion));
        doc.add(new Paragraph("● Garantía: " + garantiaStr, fCondicion));
        doc.add(new Paragraph("● NOTA: " + nota, fCondicion));
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Condiciones importantes a tomar en cuenta:", fNegrita));
        doc.add(new Paragraph("1. Aqua Solution válida la garantía de sus equipos si es instalado por nuestro personal técnico.", fCondicion));
        doc.add(new Paragraph("2. Se contempla materiales para medidas según caseta construida, materiales extras se enviará presupuesto para aprobación.", fCondicion));
        doc.add(new Paragraph("3. Aqua Solution se presentará a instalar cuando la caseta tenga corriente eléctrica, mangas de succión y descarga ya instaladas.", fCondicion));
        doc.add(new Paragraph("4. Aqua Solution entrega el equipo funcionando y se instruye al personal asignado según el cliente.", fCondicion));
        doc.add(Chunk.NEWLINE);

        if (c.getObservaciones() != null && !c.getObservaciones().isEmpty()) {
            doc.add(new Paragraph(c.getObservaciones(), fNormal));
            doc.add(Chunk.NEWLINE);
        }

        doc.add(new Paragraph("Aqua Solution agradece la oportunidad de poder ofertar una solución para las necesidades de su proyecto, estamos seguros que llenaremos sus expectativas.", fNormal));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Cualquier duda o comentario estamos dispuestos a resolverlas con gusto.", fNormal));
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        Paragraph firmaTitulo = new Paragraph("AQUA SOLUTION TU MEJOR OPCIÓN", fTotal);
        firmaTitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(firmaTitulo);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(80);
        firmas.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell fAqua = new PdfPCell(new Phrase("____________________________\n       Aqua Solution", fNormal));
        fAqua.setBorder(Rectangle.NO_BORDER);
        fAqua.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell fCliente = new PdfPCell(new Phrase("____________________________\n            Cliente", fNormal));
        fCliente.setBorder(Rectangle.NO_BORDER);
        fCliente.setHorizontalAlignment(Element.ALIGN_CENTER);
        firmas.addCell(fAqua);
        firmas.addCell(fCliente);
        doc.add(firmas);
        doc.add(Chunk.NEWLINE);

        PdfPTable pie = new PdfPTable(4);
        pie.setWidthPercentage(100);
        pie.setSpacingBefore(10);
        String[] pieData = {"☎ 4176 1927", "☎ 2434 9471 / 2434 9652",
                "✉ aquasolution.asesoria@gmail.com", "@ @aquasolution"};
        for (String p : pieData) {
            PdfPCell pc = new PdfPCell(new Phrase(p, fPie));
            pc.setBorder(Rectangle.TOP);
            pc.setPadding(5);
            pc.setHorizontalAlignment(Element.ALIGN_CENTER);
            pie.addCell(pc);
        }
        doc.add(pie);

        doc.close();
        return baos.toByteArray();
    }

    private String convertirNumeroALetras(BigDecimal numero) {
        long entero = numero.longValue();
        long centavos = numero.remainder(BigDecimal.ONE)
                .multiply(new BigDecimal(100)).longValue();
        String[] unidades = {"", "UN", "DOS", "TRES", "CUATRO", "CINCO",
                "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ", "ONCE", "DOCE",
                "TRECE", "CATORCE", "QUINCE", "DIECISÉIS", "DIECISIETE",
                "DIECIOCHO", "DIECINUEVE"};
        String[] decenas = {"", "", "VEINTE", "TREINTA", "CUARENTA",
                "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
        String[] centenas = {"", "CIEN", "DOSCIENTOS", "TRESCIENTOS",
                "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS",
                "OCHOCIENTOS", "NOVECIENTOS"};

        if (entero == 0) return "CERO QUETZALES CON " + String.format("%02d", centavos) + "/100";

        String resultado = "";
        if (entero >= 1000) {
            long miles = entero / 1000;
            if (miles == 1) resultado += "MIL ";
            else resultado += convertirCentenas(miles, unidades, decenas, centenas) + " MIL ";
            entero = entero % 1000;
        }
        resultado += convertirCentenas(entero, unidades, decenas, centenas);
        return resultado.trim() + " QUETZALES CON " + String.format("%02d", centavos) + "/100";
    }

    private String convertirCentenas(long num, String[] unidades,
                                     String[] decenas, String[] centenas) {
        String resultado = "";
        if (num >= 100) {
            resultado += centenas[(int)(num / 100)] + " ";
            num = num % 100;
        }
        if (num >= 20) {
            resultado += decenas[(int)(num / 10)];
            if (num % 10 != 0) resultado += " Y " + unidades[(int)(num % 10)];
        } else if (num > 0) {
            resultado += unidades[(int)num];
        }
        return resultado;
    }
}