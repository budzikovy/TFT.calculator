package tftcalculator.tftcalculator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tftcalculator.tftcalculator.data.dto.ChampionDto;
import tftcalculator.tftcalculator.data.dto.TeamCompositionDto;
import tftcalculator.tftcalculator.data.dto.TeamCompositionRequestDto;
import tftcalculator.tftcalculator.service.ChampionService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/champions")
public class ChampionController {

    private final ChampionService championService;

    @GetMapping("/tft14/champions")
    public ResponseEntity<List<ChampionDto>> getTFT14Champions() throws IOException {
        return ResponseEntity.ok(championService.getTFT14Champions());
    }

    @GetMapping("/tft14/traits")
    public ResponseEntity<Set<String>> getTFT14Traits() throws IOException {
        return ResponseEntity.ok(championService.getTFT14Traits());
    }

    @PostMapping("/tft14/composition")
    public ResponseEntity<TeamCompositionDto> getTeamComposition(
            @Valid @RequestBody TeamCompositionRequestDto request) throws IOException {
        return ResponseEntity.ok(championService.findOptimalTeamComposition(request));
    }
}
