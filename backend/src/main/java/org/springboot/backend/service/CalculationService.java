package org.springboot.backend.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/** Deterministic calculations only; an AI response never supplies the final result. */
@Service
public class CalculationService {

    public Map<String, Object> calculate(AiService.Analysis analysis) {
        if (!analysis.topics().contains("SETBACK")
                || analysis.roadWidthM() == null
                || analysis.buildingHeightM() == null
                || analysis.buildingHeightM() > 12.5) {
            return Map.of();
        }
        BigDecimal roadWidth = BigDecimal.valueOf(analysis.roadWidthM());
        BigDecimal result;
        String band;
        if (roadWidth.compareTo(BigDecimal.valueOf(9)) <= 0) {
            result = BigDecimal.valueOf(3.0);
            band = "road width <= 9 m";
        } else if (roadWidth.compareTo(BigDecimal.valueOf(12)) <= 0) {
            result = BigDecimal.valueOf(3.6);
            band = "road width > 9 m and <= 12 m";
        } else if (roadWidth.compareTo(BigDecimal.valueOf(18)) <= 0) {
            result = BigDecimal.valueOf(4.5);
            band = "road width > 12 m and <= 18 m";
        } else {
            result = BigDecimal.valueOf(6.0);
            band = "road width > 18 m";
        }
        Map<String, Object> calculation = new LinkedHashMap<>();
        calculation.put("code", "RESIDENTIAL_FRONT_OPEN_SPACE");
        calculation.put("rule", "56(1)(a)");
        calculation.put("matchedBand", band);
        calculation.put("roadWidthMeters", roadWidth);
        calculation.put("frontOpenSpaceMeters", result);
        return Map.copyOf(calculation);
    }
}
