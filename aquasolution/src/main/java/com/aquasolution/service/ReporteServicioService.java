package com.aquasolution.service;

import com.aquasolution.model.MaterialUtilizado;
import com.aquasolution.model.ReporteServicio;
import com.aquasolution.model.Usuario;
import com.aquasolution.repository.ReporteServicioRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ReporteServicioService {

    private final ReporteServicioRepository reporteRepository;
    private final AuditoriaService auditoriaService;

    public ReporteServicioService(ReporteServicioRepository reporteRepository, AuditoriaService auditoriaService) {
        this.reporteRepository = reporteRepository;
        this.auditoriaService = auditoriaService;
    }

    public ReporteServicio guardar(ReporteServicio reporte, String usuarioActual, String rolActual) {
        boolean esNuevo = (reporte.getId() == null);
        if (reporte.getNumeroReporte() == null) {
            long count = reporteRepository.count() + 1;
            reporte.setNumeroReporte(String.format("%06d", count));
        }
        if (reporte.getMateriales() != null) {
            for (MaterialUtilizado material : reporte.getMateriales()) {
                material.setReporte(reporte);
            }
        }
        ReporteServicio guardado = reporteRepository.save(reporte);
        if (esNuevo) {
            auditoriaService.registrar(usuarioActual, rolActual, "CREATE", "Reportes",
                    "Se creó el reporte #" + guardado.getNumeroReporte()
                            + " - Tipo: " + guardado.getTipoServicio().name()
                            + " - Cliente: " + guardado.getCliente().getNombreCompleto());
        } else {
            auditoriaService.registrar(usuarioActual, rolActual, "UPDATE", "Reportes",
                    "Se editó el reporte #" + guardado.getNumeroReporte());
        }
        return guardado;
    }

    public ReporteServicio guardar(ReporteServicio reporte) {
        return guardar(reporte, "Sistema", "SISTEMA");
    }

    public List<ReporteServicio> obtenerTodos() {
        return reporteRepository.findAll();
    }

    public Optional<ReporteServicio> obtenerPorId(Long id) {
        return reporteRepository.findById(id);
    }

    public List<ReporteServicio> obtenerPorCliente(Usuario cliente) {
        return reporteRepository.findByCliente(cliente);
    }

    public List<ReporteServicio> obtenerPorTecnico(Usuario tecnico) {
        return reporteRepository.findByTecnico(tecnico);
    }

    public void eliminar(Long id, String usuarioActual, String rolActual) {
        reporteRepository.findById(id).ifPresent(r ->
                auditoriaService.registrar(usuarioActual, rolActual, "DELETE", "Reportes",
                        "Se eliminó el reporte #" + r.getNumeroReporte()
                                + " - Cliente: " + r.getCliente().getNombreCompleto())
        );
        reporteRepository.deleteById(id);
    }

    public void eliminar(Long id) {
        eliminar(id, "Sistema", "SISTEMA");
    }

    public byte[] generarPDF(Long id) throws Exception {
        ReporteServicio r = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

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
        Font fPie = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);

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

        PdfPCell cReporte = new PdfPCell();
        cReporte.setBackgroundColor(azulOscuro);
        cReporte.setPadding(15);
        cReporte.setBorder(Rectangle.NO_BORDER);
        cReporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Font fRepLabel = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        Font fRepNum = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(173, 216, 230));
        Paragraph pReporte = new Paragraph("REPORTE DE SERVICIO\n", fRepLabel);
        pReporte.add(new Phrase("No. " + r.getNumeroReporte(), fRepNum));
        cReporte.addElement(pReporte);
        header.addCell(cReporte);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);

        PdfPCell cCliente = new PdfPCell();
        cCliente.setBorder(Rectangle.BOX);
        cCliente.setPadding(8);
        cCliente.setBackgroundColor(grisClaro);
        Paragraph pCliente = new Paragraph();
        pCliente.add(new Phrase("NOMBRE CLIENTE: ", fNegrita));
        pCliente.add(new Phrase(r.getCliente().getNombreCompleto() + "\n", fNormal));
        pCliente.add(new Phrase("DIRECCIÓN: ", fNegrita));
        pCliente.add(new Phrase(r.getPiscina() != null ? r.getPiscina().getUbicacion() : "N/A", fNormal));
        cCliente.addElement(pCliente);
        infoTable.addCell(cCliente);

        PdfPCell cFecha = new PdfPCell();
        cFecha.setBorder(Rectangle.BOX);
        cFecha.setPadding(8);
        cFecha.setBackgroundColor(grisClaro);
        Paragraph pFecha = new Paragraph();
        pFecha.add(new Phrase("FECHA: ", fNegrita));
        pFecha.add(new Phrase(r.getFechaServicio().format(fmt) + "\n", fNormal));
        pFecha.add(new Phrase("TÉCNICO: ", fNegrita));
        pFecha.add(new Phrase(r.getTecnico().getNombreCompleto() + "\n", fNormal));
        pFecha.add(new Phrase("TIPO DE SERVICIO: ", fNegrita));
        pFecha.add(new Phrase(r.getTipoServicio().name().replace("_", " "), fNormal));
        cFecha.addElement(pFecha);
        infoTable.addCell(cFecha);
        doc.add(infoTable);
        doc.add(Chunk.NEWLINE);

        PdfPTable equipoTable = new PdfPTable(2);
        equipoTable.setWidthPercentage(100);

        PdfPCell eHeader = new PdfPCell(new Phrase("DATOS DEL EQUIPO", fBlanco));
        eHeader.setColspan(2);
        eHeader.setBackgroundColor(azul);
        eHeader.setPadding(7);
        eHeader.setBorder(Rectangle.BOX);
        equipoTable.addCell(eHeader);

        switch (r.getTipoServicio()) {
            case PISCINA_JACUZZI:
                agregarCampoEquipo(equipoTable, "Bomba:", r.getBombaPiscina(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Tipo de Filtro:", r.getFiltroTipo(), fNegrita, fNormal, BaseColor.WHITE);
                agregarCampoEquipo(equipoTable, "Tamaño del Filtro:", r.getFiltroTamanio(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Lámparas:", r.getLamparas(), fNegrita, fNormal, BaseColor.WHITE);
                agregarCampoEquipo(equipoTable, "Calentador:", r.getCalentador(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Cantidad de Válvulas:", r.getCantidadValvulas(), fNegrita, fNormal, BaseColor.WHITE);
                agregarCampoEquipo(equipoTable, "Diámetro de Válvulas:", r.getDiametroValvulas(), fNegrita, fNormal, grisClaro);
                break;
            case HIDRONEUMATICO:
            case BOMBA_GENERAL:
                agregarCampoEquipo(equipoTable, "Bomba:", r.getBombaHidro(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Tanque Hidroneumático:", r.getTanqueHidroneumatico(), fNegrita, fNormal, BaseColor.WHITE);
                agregarCampoEquipo(equipoTable, "Voltaje:", r.getVoltajeHidro(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Amperaje:", r.getAmperajeHidro(), fNegrita, fNormal, BaseColor.WHITE);
                break;
            case POZO:
                agregarCampoEquipo(equipoTable, "Diámetro del Pozo:", r.getDiametroPozo(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Diámetro de Tubería:", r.getDiametroTuberia(), fNegrita, fNormal, BaseColor.WHITE);
                agregarCampoEquipo(equipoTable, "Cantidad de Tubos:", r.getCantidadTubos(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Potencia Bomba:", r.getPotenciaBomba(), fNegrita, fNormal, BaseColor.WHITE);
                agregarCampoEquipo(equipoTable, "Potencia Motor:", r.getPotenciaMotor(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Tipo de Fase:", r.getTipoFase(), fNegrita, fNormal, BaseColor.WHITE);
                agregarCampoEquipo(equipoTable, "Voltaje:", r.getVoltajePozo(), fNegrita, fNormal, grisClaro);
                agregarCampoEquipo(equipoTable, "Calibre de Cable:", r.getCalibreCable(), fNegrita, fNormal, BaseColor.WHITE);
                break;
            default:
                PdfPCell cOtro = new PdfPCell(new Phrase("Ver trabajo realizado", fNormal));
                cOtro.setColspan(2);
                cOtro.setPadding(8);
                cOtro.setBorder(Rectangle.BOX);
                equipoTable.addCell(cOtro);
        }
        doc.add(equipoTable);
        doc.add(Chunk.NEWLINE);

        PdfPTable matTable = new PdfPTable(3);
        matTable.setWidthPercentage(100);
        matTable.setWidths(new float[]{1.5f, 5, 3});

        PdfPCell matHeader = new PdfPCell(new Phrase("MATERIALES UTILIZADOS", fBlanco));
        matHeader.setColspan(3);
        matHeader.setBackgroundColor(azul);
        matHeader.setPadding(7);
        matHeader.setBorder(Rectangle.BOX);
        matTable.addCell(matHeader);

        String[] mHeaders = {"CANTIDAD", "DESCRIPCIÓN", "NOTAS"};
        for (String h : mHeaders) {
            PdfPCell hc = new PdfPCell(new Phrase(h, fNegrita));
            hc.setBackgroundColor(grisClaro);
            hc.setPadding(6);
            hc.setBorder(Rectangle.BOX);
            matTable.addCell(hc);
        }

        if (r.getMateriales() != null && !r.getMateriales().isEmpty()) {
            boolean alt = false;
            for (MaterialUtilizado m : r.getMateriales()) {
                BaseColor bg = alt ? grisClaro : BaseColor.WHITE;
                PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(m.getCantidad()), fNormal));
                PdfPCell c2 = new PdfPCell(new Phrase(m.getDescripcion(), fNormal));
                PdfPCell c3 = new PdfPCell(new Phrase(m.getNotas() != null ? m.getNotas() : "", fNormal));
                for (PdfPCell c : new PdfPCell[]{c1, c2, c3}) {
                    c.setBackgroundColor(bg);
                    c.setPadding(6);
                    c.setBorder(Rectangle.BOX);
                }
                matTable.addCell(c1);
                matTable.addCell(c2);
                matTable.addCell(c3);
                alt = !alt;
            }
        } else {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 3; j++) {
                    PdfPCell ec = new PdfPCell(new Phrase(" "));
                    ec.setPadding(10);
                    ec.setBorder(Rectangle.BOX);
                    matTable.addCell(ec);
                }
            }
        }
        doc.add(matTable);
        doc.add(Chunk.NEWLINE);

        if (r.getTrabajoRealizado() != null && !r.getTrabajoRealizado().isEmpty()) {
            PdfPTable trabajoTable = new PdfPTable(1);
            trabajoTable.setWidthPercentage(100);
            PdfPCell tHeader = new PdfPCell(new Phrase("TRABAJO REALIZADO", fBlanco));
            tHeader.setBackgroundColor(azul);
            tHeader.setPadding(7);
            tHeader.setBorder(Rectangle.BOX);
            trabajoTable.addCell(tHeader);
            PdfPCell tContent = new PdfPCell(new Phrase(r.getTrabajoRealizado(), fNormal));
            tContent.setPadding(8);
            tContent.setBorder(Rectangle.BOX);
            tContent.setMinimumHeight(50);
            trabajoTable.addCell(tContent);
            doc.add(trabajoTable);
            doc.add(Chunk.NEWLINE);
        }

        PdfPTable obsTable = new PdfPTable(2);
        obsTable.setWidthPercentage(100);
        PdfPCell obsHeader = new PdfPCell(new Phrase("OBSERVACIONES", fBlanco));
        obsHeader.setBackgroundColor(azul);
        obsHeader.setPadding(7);
        obsHeader.setBorder(Rectangle.BOX);
        PdfPCell proxHeader = new PdfPCell(new Phrase("PRÓXIMA VISITA", fBlanco));
        proxHeader.setBackgroundColor(azul);
        proxHeader.setPadding(7);
        proxHeader.setBorder(Rectangle.BOX);
        obsTable.addCell(obsHeader);
        obsTable.addCell(proxHeader);

        PdfPCell obsContent = new PdfPCell(new Phrase(
                r.getObservaciones() != null ? r.getObservaciones() : "", fNormal));
        obsContent.setPadding(8);
        obsContent.setBorder(Rectangle.BOX);
        obsContent.setMinimumHeight(40);
        PdfPCell proxContent = new PdfPCell(new Phrase(
                r.getProximaVisita() != null ? r.getProximaVisita() : "", fNormal));
        proxContent.setPadding(8);
        proxContent.setBorder(Rectangle.BOX);
        proxContent.setMinimumHeight(40);
        obsTable.addCell(obsContent);
        obsTable.addCell(proxContent);
        doc.add(obsTable);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        if (r.getFoto1() != null || r.getFoto2() != null || r.getFoto3() != null) {
            PdfPTable fotosHeaderTable = new PdfPTable(1);
            fotosHeaderTable.setWidthPercentage(100);
            PdfPCell fotosHeader = new PdfPCell(new Phrase("EVIDENCIA FOTOGRAFICA", fBlanco));
            fotosHeader.setBackgroundColor(azul);
            fotosHeader.setPadding(7);
            fotosHeader.setBorder(Rectangle.BOX);
            fotosHeaderTable.addCell(fotosHeader);
            doc.add(fotosHeaderTable);

            String uploadDir = System.getProperty("user.dir") + "/uploads/reportes/";
            int fotosCount = (r.getFoto1() != null ? 1 : 0) +
                    (r.getFoto2() != null ? 1 : 0) +
                    (r.getFoto3() != null ? 1 : 0);

            PdfPTable fotosTable = new PdfPTable(fotosCount);
            fotosTable.setWidthPercentage(100);
            fotosTable.setSpacingBefore(5);

            String[] fotos = {r.getFoto1(), r.getFoto2(), r.getFoto3()};
            String[] etiquetas = {"Antes del tratamiento", "Despues del tratamiento", "Equipo / Instalacion"};

            for (int i = 0; i < fotos.length; i++) {
                if (fotos[i] != null) {
                    try {
                        java.io.File fotoFile = new java.io.File(uploadDir + fotos[i]);
                        if (fotoFile.exists()) {
                            Image img = Image.getInstance(fotoFile.getAbsolutePath());
                            img.scaleToFit(160, 130);
                            PdfPCell imgCell = new PdfPCell();
                            imgCell.addElement(img);
                            imgCell.setBorder(Rectangle.BOX);
                            imgCell.setPadding(5);
                            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            Paragraph etiqueta = new Paragraph(etiquetas[i], fNormal);
                            etiqueta.setAlignment(Element.ALIGN_CENTER);
                            imgCell.addElement(etiqueta);
                            fotosTable.addCell(imgCell);
                        }
                    } catch (Exception e) {
                        // Si falla la foto continúa sin ella
                    }
                }
            }
            doc.add(fotosTable);
            doc.add(Chunk.NEWLINE);
        }

        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(80);
        firmas.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell fCliente = new PdfPCell(new Phrase(
                "____________________________\n         CLIENTE", fNormal));
        fCliente.setBorder(Rectangle.NO_BORDER);
        fCliente.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell fTecnico = new PdfPCell(new Phrase(
                "____________________________\n   TÉCNICO AUTORIZADO:\n   " +
                        r.getTecnico().getNombreCompleto(), fNormal));
        fTecnico.setBorder(Rectangle.NO_BORDER);
        fTecnico.setHorizontalAlignment(Element.ALIGN_CENTER);
        firmas.addCell(fCliente);
        firmas.addCell(fTecnico);
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

    private void agregarCampoEquipo(PdfPTable tabla, String label, String valor,
                                    Font fNegrita, Font fNormal, BaseColor bg) {
        PdfPCell lc = new PdfPCell(new Phrase(label, fNegrita));
        lc.setBorder(Rectangle.BOX);
        lc.setPadding(6);
        lc.setBackgroundColor(bg);
        PdfPCell vc = new PdfPCell(new Phrase(valor != null ? valor : "", fNormal));
        vc.setBorder(Rectangle.BOX);
        vc.setPadding(6);
        vc.setBackgroundColor(bg);
        tabla.addCell(lc);
        tabla.addCell(vc);
    }
}