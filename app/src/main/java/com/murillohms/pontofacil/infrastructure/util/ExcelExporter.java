package com.murillohms.pontofacil.infrastructure.util;

import android.content.Context;
import android.os.Environment;

import com.murillohms.pontofacil.domain.entity.FuncionarioEntity;
import com.murillohms.pontofacil.domain.entity.RegistroPontoEntity;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelExporter {

    private Context context;

    public ExcelExporter(Context context) {
        this.context = context;
    }

    public File exportarRelatorioMensal(FuncionarioEntity funcionario,
                                        List<RegistroPontoEntity> registros,
                                        String mesAno) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório de Ponto - " + mesAno);

        // Estilos
        CellStyle headerStyle = criarEstiloHeader(workbook);
        CellStyle infoStyle = criarEstiloInfo(workbook);
        CellStyle dataStyle = criarEstiloData(workbook);
        CellStyle totalStyle = criarEstiloTotal(workbook);

        int rowNum = 0;

        rowNum = criarCabecalhoEmpresa(sheet, funcionario, mesAno, infoStyle, rowNum);

        rowNum++;

        rowNum = criarCabecalhoTabela(sheet, headerStyle, rowNum);

        int totalHoras = 0;
        int totalMinutos = 0;

        for (RegistroPontoEntity registro : registros) {
            Row row = sheet.createRow(rowNum++);

            createCell(row, 0, registro.getData(), dataStyle);
            createCell(row, 1, registro.getEntrada() != null ? registro.getEntrada() : "--", dataStyle);
            createCell(row, 2, registro.getAlmocoSaida() != null ? registro.getAlmocoSaida() : "--", dataStyle);
            createCell(row, 3, registro.getAlmocoRetorno() != null ? registro.getAlmocoRetorno() : "--", dataStyle);
            createCell(row, 4, registro.getSaida() != null ? registro.getSaida() : "--", dataStyle);

            String horasTrabalhadas = registro.calcularHorasTrabalhadas();
            createCell(row, 5, horasTrabalhadas, dataStyle);

            if (!horasTrabalhadas.equals("--:--")) {
                String[] parts = horasTrabalhadas.split(":");
                totalHoras += Integer.parseInt(parts[0]);
                totalMinutos += Integer.parseInt(parts[1]);
            }

            createCell(row, 6, registro.getObservacao() != null ? registro.getObservacao() : "", dataStyle);
        }

        totalHoras += totalMinutos / 60;
        totalMinutos = totalMinutos % 60;

        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        createCell(totalRow, 0, "TOTAL MENSAL:", totalStyle);
        createCell(totalRow, 5, String.format("%02d:%02d", totalHoras, totalMinutos), totalStyle);

        rowNum += 2;
        criarRodape(sheet, registros.size(), totalHoras, totalMinutos, infoStyle, rowNum);

        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }

        return salvarArquivo(workbook, funcionario, mesAno);
    }

    private int criarCabecalhoEmpresa(Sheet sheet, FuncionarioEntity funcionario,
                                      String mesAno, CellStyle style, int rowNum) {
        Row row;

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "RELATÓRIO DE PONTO ELETRÔNICO", style);

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "Empresa: " + funcionario.getEmpresa(), style);

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "CNPJ: " + funcionario.getCnpj(), style);

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "Funcionário: " + funcionario.getNome(), style);

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "Matrícula: " + funcionario.getMatricula(), style);

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "CPF: " + funcionario.getCpf(), style);

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "Cargo: " + funcionario.getCargo(), style);

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "Período: " + mesAno, style);

        return rowNum;
    }

    private int criarCabecalhoTabela(Sheet sheet, CellStyle style, int rowNum) {
        Row headerRow = sheet.createRow(rowNum++);

        createCell(headerRow, 0, "Data", style);
        createCell(headerRow, 1, "Entrada", style);
        createCell(headerRow, 2, "Saída Almoço", style);
        createCell(headerRow, 3, "Retorno Almoço", style);
        createCell(headerRow, 4, "Saída", style);
        createCell(headerRow, 5, "Horas Trabalhadas", style);
        createCell(headerRow, 6, "Observações", style);

        return rowNum;
    }

    private void criarRodape(Sheet sheet, int diasTrabalhados, int totalHoras,
                             int totalMinutos, CellStyle style, int rowNum) {
        Row row;

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "Dias Trabalhados: " + diasTrabalhados, style);

        row = sheet.createRow(rowNum++);
        createCell(row, 0, "Total de Horas: " + totalHoras + "h " + totalMinutos + "min", style);

        double mediaDiaria = diasTrabalhados > 0 ?
                ((totalHoras * 60 + totalMinutos) / (double) diasTrabalhados) / 60 : 0;

        row = sheet.createRow(rowNum++);
        createCell(row, 0, String.format("Média Diária: %.2fh", mediaDiaria), style);

        row = sheet.createRow(rowNum + 1);
        String dataGeracao = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        createCell(row, 0, "Relatório gerado em: " + dataGeracao, style);
    }

    private File salvarArquivo(Workbook workbook, FuncionarioEntity funcionario, String mesAno) throws Exception {
        String nomeArquivo = String.format("Ponto_%s_%s.xlsx",
                funcionario.getNome().replace(" ", "_"),
                mesAno.replace("/", "-"));

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, nomeArquivo);

        FileOutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();

        return file;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle criarEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle criarEstiloInfo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle criarEstiloData(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle criarEstiloTotal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        return style;
    }
}