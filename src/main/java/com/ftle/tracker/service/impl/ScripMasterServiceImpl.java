package com.ftle.tracker.service.impl;

import com.ftle.tracker.dto.ScripMasterDto;
import com.ftle.tracker.service.ScripMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScripMasterServiceImpl implements ScripMasterService {
    private final WebClient webTextClient;

    @Cacheable(value = "scripMasterCache")
    public List<ScripMasterDto> getScripMasterData() {
        log.info("Served from api");
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        String url = String.format(
                "https://lapi.kotaksecurities.com/wso2-scripmaster/v1/prod/%s/transformed-v1/nse_cm-v1.csv",
                date
        );

        String csvData = webTextClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (csvData == null || csvData.isEmpty()) {
            throw new RuntimeException("Empty CSV response from Kotak API");
        }

        return parseCsv(csvData);
    }

    private List<ScripMasterDto> parseCsv(String csvData) {

        List<ScripMasterDto> result = new ArrayList<>();

        try (
                Reader reader = new StringReader(csvData);
                CSVParser csvParser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim()
                        .parse(reader)
        ) {
            for (CSVRecord record : csvParser) {

                ScripMasterDto dto = new ScripMasterDto();
                dto.setPSymbol(record.get("pSymbol"));
                dto.setPGroup(record.get("pGroup"));
                dto.setPExchSeg(record.get("pExchSeg"));
                dto.setPSymbolName(record.get("pSymbolName"));
                dto.setPTrdSymbol(record.get("pTrdSymbol"));
                dto.setPDesc(record.get("pDesc"));

                result.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("CSV parsing failed", e);
        }

        return result;
    }
}