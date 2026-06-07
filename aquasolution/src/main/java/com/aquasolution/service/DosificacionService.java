package com.aquasolution.service;

import com.aquasolution.model.Dosificacion;
import com.aquasolution.model.Piscina;
import com.aquasolution.model.Usuario;
import com.aquasolution.repository.DosificacionRepository;
import com.aquasolution.util.CalculoHidraulicoUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class DosificacionService {

    private final DosificacionRepository dosificacionRepository;
    private final CalculoHidraulicoUtil calculoUtil;
    private final AuditoriaService auditoriaService;

    public DosificacionService(DosificacionRepository dosificacionRepository,
                               CalculoHidraulicoUtil calculoUtil,
                               AuditoriaService auditoriaService) {
        this.dosificacionRepository = dosificacionRepository;
        this.calculoUtil = calculoUtil;
        this.auditoriaService = auditoriaService;
    }

    public Dosificacion guardar(Dosificacion dosificacion, String usuarioActual, String rolActual) {
        boolean esNueva = (dosificacion.getId() == null);
        if (dosificacion.getNumeroDosificacion() == null) {
            LocalDateTime now = LocalDateTime.now();
            String numero = "DOS" + String.format("%02d", now.getDayOfMonth())
                    + String.format("%02d", now.getMonthValue())
                    + now.getYear();
            String numeroFinal = numero;
            int intentos = 0;
            while (dosificacionRepository.existsByNumeroDosificacion(numeroFinal)) {
                intentos++;
                numeroFinal = numero + "-" + intentos;
            }
            dosificacion.setNumeroDosificacion(numeroFinal);
        }
        Dosificacion guardada = dosificacionRepository.save(dosificacion);
        if (esNueva) {
            auditoriaService.registrar(usuarioActual, rolActual, "CREATE", "Dosificaciones",
                    "Se creó dosificación " + guardada.getNumeroDosificacion()
                            + " - Piscina: " + guardada.getPiscina().getNombre()
                            + " - Cliente: " + guardada.getCliente().getNombreCompleto());
        } else {
            auditoriaService.registrar(usuarioActual, rolActual, "UPDATE", "Dosificaciones",
                    "Se editó dosificación " + guardada.getNumeroDosificacion());
        }
        return guardada;
    }

    public Dosificacion guardar(Dosificacion dosificacion) {
        return guardar(dosificacion, "Sistema", "SISTEMA");
    }

    public List<Dosificacion> obtenerTodas() {
        return dosificacionRepository.findAll();
    }

    public Optional<Dosificacion> obtenerPorId(Long id) {
        return dosificacionRepository.findById(id);
    }

    public List<Dosificacion> obtenerPorPiscina(Piscina piscina) {
        return dosificacionRepository.findByPiscina(piscina);
    }

    public List<Dosificacion> obtenerPorCliente(Usuario cliente) {
        return dosificacionRepository.findByCliente(cliente);
    }

    public void eliminar(Long id, String usuarioActual, String rolActual) {
        dosificacionRepository.findById(id).ifPresent(d ->
                auditoriaService.registrar(usuarioActual, rolActual, "DELETE", "Dosificaciones",
                        "Se eliminó dosificación " + d.getNumeroDosificacion()
                                + " - Cliente: " + d.getCliente().getNombreCompleto())
        );
        dosificacionRepository.deleteById(id);
    }

    public void eliminar(Long id) {
        eliminar(id, "Sistema", "SISTEMA");
    }

    public byte[] generarPDF(Long id) throws Exception {
        Dosificacion d = dosificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dosificacion no encontrada"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 80, 50);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        BaseColor azul = new BaseColor(30, 77, 140);
        BaseColor azulOscuro = new BaseColor(26, 61, 110);
        BaseColor verde = new BaseColor(16, 110, 86);
        BaseColor amarillo = new BaseColor(146, 64, 14);
        BaseColor rojo = new BaseColor(153, 27, 27);
        BaseColor bgVerde = new BaseColor(209, 250, 229);
        BaseColor bgAmarillo = new BaseColor(254, 243, 199);
        BaseColor bgRojo = new BaseColor(254, 226, 226);
        BaseColor grisClaro = new BaseColor(240, 244, 248);

        Font fNormal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
        Font fNegrita = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.DARK_GRAY);
        Font fBlanco = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        Font fTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
        Font fSubtitulo = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE);
        Font fPie = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
        Font fSeccion = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        Font fDosis = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, azul);

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3, 2});

        PdfPCell cEmpresa = new PdfPCell();
        cEmpresa.setBackgroundColor(azul);
        cEmpresa.setPadding(15);
        cEmpresa.setBorder(Rectangle.NO_BORDER);
        Paragraph pEmpresa = new Paragraph("AQUA SOLUTION\n", fTitulo);
        pEmpresa.add(new Phrase("Tu mejor opcion en agua", fSubtitulo));
        cEmpresa.addElement(pEmpresa);
        header.addCell(cEmpresa);

        PdfPCell cDoc = new PdfPCell();
        cDoc.setBackgroundColor(azulOscuro);
        cDoc.setPadding(15);
        cDoc.setBorder(Rectangle.NO_BORDER);
        cDoc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Font fDocLabel = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        Font fDocNum = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(173, 216, 230));
        Paragraph pDoc = new Paragraph("REPORTE DE DOSIFICACION\n", fDocLabel);
        pDoc.add(new Phrase("No. " + d.getNumeroDosificacion(), fDocNum));
        cDoc.addElement(pDoc);
        header.addCell(cDoc);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);

        PdfPCell cCliente = new PdfPCell();
        cCliente.setBorder(Rectangle.BOX);
        cCliente.setPadding(10);
        cCliente.setBackgroundColor(grisClaro);
        Paragraph pCliente = new Paragraph();
        pCliente.add(new Phrase("CLIENTE: ", fNegrita));
        pCliente.add(new Phrase(d.getCliente().getNombreCompleto() + "\n", fNormal));
        pCliente.add(new Phrase("PISCINA: ", fNegrita));
        pCliente.add(new Phrase(d.getPiscina().getNombre() + "\n", fNormal));
        pCliente.add(new Phrase("TIPO: ", fNegrita));
        pCliente.add(new Phrase(d.getPiscina().getTipo().name() + "\n", fNormal));
        pCliente.add(new Phrase("VOLUMEN: ", fNegrita));
        pCliente.add(new Phrase(d.getVolumenGalones() + " galones", fNormal));
        cCliente.addElement(pCliente);
        infoTable.addCell(cCliente);

        PdfPCell cFecha = new PdfPCell();
        cFecha.setBorder(Rectangle.BOX);
        cFecha.setPadding(10);
        cFecha.setBackgroundColor(grisClaro);
        Paragraph pFecha = new Paragraph();
        pFecha.add(new Phrase("FECHA: ", fNegrita));
        pFecha.add(new Phrase(d.getFechaCreacion().format(fmt) + "\n", fNormal));
        pFecha.add(new Phrase("TECNICO: ", fNegrita));
        pFecha.add(new Phrase(d.getTecnico().getNombreCompleto() + "\n", fNormal));
        pFecha.add(new Phrase("DESINFECTANTE: ", fNegrita));
        pFecha.add(new Phrase(d.getDesinfectante().equals("CHLOR65") ?
                "Chlor 65 (Hipoclorito de Calcio 65%)" :
                "Trichlor 90 (Acido Tricloroisocianurico 90%)", fNormal));
        cFecha.addElement(pFecha);
        infoTable.addCell(cFecha);
        doc.add(infoTable);
        doc.add(Chunk.NEWLINE);

        PdfPCell semHeader = new PdfPCell(new Phrase("ESTADO DE PARAMETROS — AquaCheck 7", fSeccion));
        semHeader.setBackgroundColor(azul);
        semHeader.setPadding(8);
        semHeader.setBorder(Rectangle.BOX);
        semHeader.setColspan(5);

        PdfPTable semTable = new PdfPTable(5);
        semTable.setWidthPercentage(100);
        semTable.addCell(semHeader);

        agregarCeldaSemaforo(semTable, "Cloro Libre",
                d.getCloroLibre().doubleValue() + " ppm", "Ideal: 2.0-4.0",
                calculoUtil.estadoCloro(d.getCloroLibre().doubleValue()),
                bgVerde, bgAmarillo, bgRojo, verde, amarillo, rojo);
        agregarCeldaSemaforo(semTable, "pH",
                d.getPh().doubleValue() + "", "Ideal: 7.4-7.6",
                calculoUtil.estadoPH(d.getPh().doubleValue()),
                bgVerde, bgAmarillo, bgRojo, verde, amarillo, rojo);

        String estadoAlc = d.getAlcalinidad() != null ?
                calculoUtil.estadoAlcalinidad(d.getAlcalinidad().doubleValue()) : "GRIS";
        agregarCeldaSemaforo(semTable, "Alcalinidad",
                d.getAlcalinidad() != null ? d.getAlcalinidad() + " ppm" : "N/A", "Ideal: 80-120",
                estadoAlc, bgVerde, bgAmarillo, bgRojo, verde, amarillo, rojo);

        String estadoCal = d.getCalcio() != null ?
                calculoUtil.estadoCalcio(d.getCalcio().doubleValue()) : "GRIS";
        agregarCeldaSemaforo(semTable, "Calcio",
                d.getCalcio() != null ? d.getCalcio() + " ppm" : "N/A", "Ideal: 200-400",
                estadoCal, bgVerde, bgAmarillo, bgRojo, verde, amarillo, rojo);

        String estadoCya = d.getCya() != null ?
                calculoUtil.estadoCYA(d.getCya().doubleValue()) : "GRIS";
        agregarCeldaSemaforo(semTable, "CYA",
                d.getCya() != null ? d.getCya() + " ppm" : "N/A", "Ideal: 30-50",
                estadoCya, bgVerde, bgAmarillo, bgRojo, verde, amarillo, rojo);
        doc.add(semTable);
        doc.add(Chunk.NEWLINE);

        if (d.getLsi() != null) {
            PdfPTable lsiTable = new PdfPTable(2);
            lsiTable.setWidthPercentage(100);
            lsiTable.setWidths(new float[]{4, 1});

            BaseColor lsiBg = bgVerde;
            Font lsiFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, verde);
            String lsiMsg = "Agua perfectamente balanceada. No corroe ni forma sarro.";

            double lsiVal = d.getLsi().doubleValue();
            if (lsiVal < -0.5) {
                lsiBg = bgRojo; lsiFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, rojo);
                lsiMsg = "Agua corrosiva. Puede danar superficies. Suba Calcio o Alcalinidad.";
            } else if (lsiVal < -0.3) {
                lsiBg = bgAmarillo; lsiFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, amarillo);
                lsiMsg = "Agua ligeramente corrosiva. Ajuste preventivo recomendado.";
            } else if (lsiVal > 0.5) {
                lsiBg = bgRojo; lsiFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, rojo);
                lsiMsg = "Agua incrustante. Forma sarro. Baje Calcio o use acido muriatico.";
            } else if (lsiVal > 0.3) {
                lsiBg = bgAmarillo; lsiFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, amarillo);
                lsiMsg = "Tendencia a formar sarro. Baje Calcio o Alcalinidad.";
            }

            PdfPCell lsiMsgCell = new PdfPCell();
            lsiMsgCell.setBackgroundColor(lsiBg);
            lsiMsgCell.setBorder(Rectangle.BOX);
            lsiMsgCell.setPadding(10);
            Paragraph pLsi = new Paragraph();
            pLsi.add(new Phrase("INDICE DE SATURACION DE LANGELIER (LSI)\n", fNegrita));
            pLsi.add(new Phrase(lsiMsg, lsiFont));
            lsiMsgCell.addElement(pLsi);
            lsiTable.addCell(lsiMsgCell);

            Font fLsiNum = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,
                    lsiVal >= -0.3 && lsiVal <= 0.3 ? verde : (Math.abs(lsiVal) > 0.5 ? rojo : amarillo));
            PdfPCell lsiValCell = new PdfPCell();
            lsiValCell.setBackgroundColor(lsiBg);
            lsiValCell.setBorder(Rectangle.BOX);
            lsiValCell.setPadding(10);
            lsiValCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            lsiValCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            lsiValCell.addElement(new Paragraph(String.valueOf(d.getLsi()), fLsiNum));
            lsiTable.addCell(lsiValCell);
            doc.add(lsiTable);
            doc.add(Chunk.NEWLINE);
        }

        PdfPTable dosisTable = new PdfPTable(3);
        dosisTable.setWidthPercentage(100);
        PdfPCell dosisHeader = new PdfPCell(new Phrase("DOSIS DE QUIMICOS RECOMENDADAS — Norma I-CPO", fSeccion));
        dosisHeader.setBackgroundColor(azul);
        dosisHeader.setPadding(8);
        dosisHeader.setBorder(Rectangle.BOX);
        dosisHeader.setColspan(3);
        dosisTable.addCell(dosisHeader);

        for (String h : new String[]{"PRODUCTO", "CANTIDAD", "REFERENCIA"}) {
            PdfPCell hc = new PdfPCell(new Phrase(h, fNegrita));
            hc.setBackgroundColor(grisClaro);
            hc.setPadding(7);
            hc.setBorder(Rectangle.BOX);
            hc.setHorizontalAlignment(Element.ALIGN_CENTER);
            dosisTable.addCell(hc);
        }

        boolean alt = false;
        if (d.getDosisDesinfectante() != null && d.getDosisDesinfectante().doubleValue() > 0) {
            String nombre = d.getDesinfectante().equals("CHLOR65") ? "Chlor 65 (Granular)" : "Trichlor 90";
            String ref = d.getDesinfectante().equals("CHLOR65") ?
                    "Formula I-CPO: 2.0 oz/10,000 gal/ppm" : "Formula I-CPO: 1.5 oz/10,000 gal/ppm";
            agregarFilaDosis(dosisTable, nombre, d.getDosisDesinfectante() + " oz", ref, alt, fNormal, fDosis);
            alt = !alt;
        }
        if (d.getDosisPhIncreaser() != null && d.getDosisPhIncreaser().doubleValue() > 0) {
            agregarFilaDosis(dosisTable, "pH Increaser (Soda Ash / Carbonato de Sodio)",
                    d.getDosisPhIncreaser() + " oz", "Para subir pH a 7.5", alt, fNormal, fDosis);
            alt = !alt;
        }
        if (d.getDosisPhDecreaser() != null && d.getDosisPhDecreaser().doubleValue() > 0) {
            agregarFilaDosis(dosisTable, "pH Decreaser (Acido Clorhidrico HCl 32%)",
                    d.getDosisPhDecreaser() + " oz", "Para bajar pH a 7.5", alt, fNormal, fDosis);
            alt = !alt;
        }
        if (d.getDosisAlgicida() != null && d.getDosisAlgicida().doubleValue() > 0) {
            String dosisAlgLabel = "Alguicida";
            if (d.getAspecto().equals("ALGAS_NEGRAS")) dosisAlgLabel = "Alguicida (Dosis triple — Algas negras/mostaza)";
            else if (d.getAspecto().equals("ALGAS_VERDES")) dosisAlgLabel = "Alguicida (Dosis doble — Algas verdes)";
            agregarFilaDosis(dosisTable, dosisAlgLabel, d.getDosisAlgicida() + " fl.oz",
                    "1 fl.oz / 1,550 gal (base I-CPO)", alt, fNormal, fDosis);
            alt = !alt;
        }
        if (d.getDosisClarificador() != null && d.getDosisClarificador().doubleValue() > 0) {
            String clarifNombre = d.getAspecto().equals("LIGERAMENTE_TURBIA") ?
                    "Clarificador Super Blue" : "Clarificador ClearAqua (dosis doble)";
            String clarifRef = d.getAspecto().equals("LIGERAMENTE_TURBIA") ?
                    "1 fl.oz / 5,000 gal" : "0.38 fl.oz / 1,000 gal x2";
            agregarFilaDosis(dosisTable, clarifNombre, d.getDosisClarificador() + " fl.oz",
                    clarifRef, alt, fNormal, fDosis);
        }
        doc.add(dosisTable);
        doc.add(Chunk.NEWLINE);

        PdfPTable ordenTable = new PdfPTable(2);
        ordenTable.setWidthPercentage(100);
        ordenTable.setWidths(new float[]{1, 8});
        PdfPCell ordenHeader = new PdfPCell(new Phrase("ORDEN SEGURO DE APLICACION (I-CPO)", fSeccion));
        ordenHeader.setBackgroundColor(azul);
        ordenHeader.setPadding(8);
        ordenHeader.setBorder(Rectangle.BOX);
        ordenHeader.setColspan(2);
        ordenTable.addCell(ordenHeader);

        String[][] pasos = {
                {"1", "Ajustar Alcalinidad Total — Base del balance del agua"},
                {"2", "Ajustar pH — Con Soda Ash (subir) o Acido Muriatico (bajar)"},
                {"3", "Agregar Cloro o Trichlor — El pH correcto maximiza la eficacia"},
                {"4", "Agregar Clarificador — Si el agua esta turbia"},
                {"5", "Agregar Alguicida — Si hay presencia de algas"},
                {"!", "NUNCA mezclar quimicos directamente — Pre-disolver en agua separada"}
        };
        for (int i = 0; i < pasos.length; i++) {
            BaseColor numBg = i == pasos.length - 1 ? rojo : azul;
            PdfPCell numCell = new PdfPCell(new Phrase(pasos[i][0], fBlanco));
            numCell.setBackgroundColor(numBg);
            numCell.setPadding(7);
            numCell.setBorder(Rectangle.BOX);
            numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            ordenTable.addCell(numCell);
            Font fPaso = i == pasos.length - 1 ?
                    new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, rojo) : fNormal;
            PdfPCell pasoCell = new PdfPCell(new Phrase(pasos[i][1], fPaso));
            pasoCell.setBackgroundColor(i % 2 == 0 ? BaseColor.WHITE : grisClaro);
            pasoCell.setPadding(7);
            pasoCell.setBorder(Rectangle.BOX);
            ordenTable.addCell(pasoCell);
        }
        doc.add(ordenTable);
        doc.add(Chunk.NEWLINE);

        if (d.getObservaciones() != null && !d.getObservaciones().isEmpty()) {
            PdfPTable obsTable = new PdfPTable(1);
            obsTable.setWidthPercentage(100);
            PdfPCell obsHeader2 = new PdfPCell(new Phrase("OBSERVACIONES", fSeccion));
            obsHeader2.setBackgroundColor(azul);
            obsHeader2.setPadding(8);
            obsHeader2.setBorder(Rectangle.BOX);
            obsTable.addCell(obsHeader2);
            PdfPCell obsContent = new PdfPCell(new Phrase(d.getObservaciones(), fNormal));
            obsContent.setPadding(10);
            obsContent.setBorder(Rectangle.BOX);
            obsContent.setMinimumHeight(40);
            obsTable.addCell(obsContent);
            doc.add(obsTable);
            doc.add(Chunk.NEWLINE);
        }

        doc.add(Chunk.NEWLINE);
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(80);
        firmas.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell fCliente2 = new PdfPCell(new Phrase(
                "____________________________\n         CLIENTE\n" +
                        d.getCliente().getNombreCompleto(), fNormal));
        fCliente2.setBorder(Rectangle.NO_BORDER);
        fCliente2.setHorizontalAlignment(Element.ALIGN_CENTER);
        fCliente2.setPadding(10);
        PdfPCell fTecnico2 = new PdfPCell(new Phrase(
                "____________________________\n   TECNICO AUTORIZADO\n   " +
                        d.getTecnico().getNombreCompleto(), fNormal));
        fTecnico2.setBorder(Rectangle.NO_BORDER);
        fTecnico2.setHorizontalAlignment(Element.ALIGN_CENTER);
        fTecnico2.setPadding(10);
        firmas.addCell(fCliente2);
        firmas.addCell(fTecnico2);
        doc.add(firmas);

        PdfPTable pie = new PdfPTable(4);
        pie.setWidthPercentage(100);
        pie.setSpacingBefore(15);
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

    private void agregarCeldaSemaforo(PdfPTable table, String parametro,
                                      String valor, String rango, String estado,
                                      BaseColor bgVerde, BaseColor bgAmarillo, BaseColor bgRojo,
                                      BaseColor verde, BaseColor amarillo, BaseColor rojo) {
        BaseColor bg = estado.equals("VERDE") ? bgVerde :
                estado.equals("AMARILLO") ? bgAmarillo : bgRojo;
        BaseColor color = estado.equals("VERDE") ? verde :
                estado.equals("AMARILLO") ? amarillo : rojo;
        String icono = estado.equals("VERDE") ? "OK" : estado.equals("AMARILLO") ? "!" : "X";

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font fParam = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, color);
        Font fValor = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, color);
        Font fRango = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY);
        Font fIcono = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, color);

        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.add(new Phrase(parametro + "\n", fParam));
        p.add(new Phrase(valor + "\n", fValor));
        p.add(new Phrase(rango + "\n", fRango));
        p.add(new Phrase(icono, fIcono));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void agregarFilaDosis(PdfPTable table, String producto,
                                  String cantidad, String referencia,
                                  boolean alt, Font fNormal, Font fDosis) {
        BaseColor bg = alt ? new BaseColor(240, 244, 248) : BaseColor.WHITE;
        PdfPCell c1 = new PdfPCell(new Phrase(producto, fNormal));
        c1.setBackgroundColor(bg); c1.setPadding(8); c1.setBorder(Rectangle.BOX);
        PdfPCell c2 = new PdfPCell(new Phrase(cantidad, fDosis));
        c2.setBackgroundColor(bg); c2.setPadding(8); c2.setBorder(Rectangle.BOX);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell c3 = new PdfPCell(new Phrase(referencia, fNormal));
        c3.setBackgroundColor(bg); c3.setPadding(8); c3.setBorder(Rectangle.BOX);
        table.addCell(c1); table.addCell(c2); table.addCell(c3);
    }
}