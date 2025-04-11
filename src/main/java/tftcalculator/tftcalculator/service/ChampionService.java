package tftcalculator.tftcalculator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tftcalculator.tftcalculator.config.TftConstants;
import tftcalculator.tftcalculator.data.dto.ChampionDto;
import tftcalculator.tftcalculator.data.dto.TeamCompositionDto;
import tftcalculator.tftcalculator.data.dto.TeamCompositionRequestDto;
import tftcalculator.tftcalculator.mapper.ChampionMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChampionService {

    private final ChampionMapper championMapper;
    private final ObjectMapper objectMapper;
    private final TftConstants tftConstants;

    public void debugJsonStructure() throws IOException {
        InputStream input = new URL("https://raw.communitydragon.org/latest/cdragon/tft/en_us.json").openStream();
        JsonNode rootArray = objectMapper.readTree(input);
        
        log.info("Root JSON structure:");
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootArray));
        
        // Przeanalizuj każdy węzeł
        for (JsonNode node : rootArray) {
            log.info("Node fields: {}", node.fieldNames().toString());
            node.fields().forEachRemaining(entry -> 
                log.info("Field: {} = {}", entry.getKey(), entry.getValue())
            );
        }
    }

    public List<ChampionDto> getTFT14Champions() throws IOException {
        InputStream input = new URL("https://raw.communitydragon.org/latest/cdragon/tft/en_us.json").openStream();
        JsonNode rootNode = objectMapper.readTree(input);
        List<ChampionDto> champions = new ArrayList<>();

        searchForChampions(rootNode, champions);
        
        log.info("Number of champions before distinct: {}", champions.size());
        
        List<ChampionDto> uniqueChampions = champions.stream()
                .filter(champion -> champion.getName() != null && champion.getTraits() != null && !champion.getTraits().isEmpty())
                .distinct()
                .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
                .collect(Collectors.toList());
                
        log.info("Number of champions after distinct: {}", uniqueChampions.size());
        
        return uniqueChampions;
    }

    private void searchForChampions(JsonNode node, List<ChampionDto> champions) {
        if (node.isObject()) {
            JsonNode characterNameNode = node.get("characterName");
            if (characterNameNode != null && characterNameNode.isTextual()) {
                String characterName = characterNameNode.asText();
                if (characterName.startsWith(tftConstants.getTftPrefix())) {
                    try {
                        log.info("Raw champion data: {}", node.toString());
                        ChampionDto dto = objectMapper.treeToValue(node, ChampionDto.class);
                        log.info("Mapped to DTO before mapper: {}", dto);
                        ChampionDto mappedDto = championMapper.toDto(dto);
                        log.info("After mapper: {}", mappedDto);
                        champions.add(mappedDto);
                    } catch (Exception e) {
                        log.error("Error mapping champion: {}", e.getMessage(), e);
                    }
                }
            }
            
            node.fields().forEachRemaining(entry -> searchForChampions(entry.getValue(), champions));
        } else if (node.isArray()) {
            node.forEach(element -> searchForChampions(element, champions));
        }
    }

    public Set<String> getTFT14Traits() throws IOException {
        return getTFT14Champions().stream()
                .flatMap(champion -> champion.getTraits().stream())
                .collect(Collectors.toCollection(TreeSet::new)); // używamy TreeSet dla automatycznego sortowania
    }

    public TeamCompositionDto findOptimalTeamComposition(TeamCompositionRequestDto request) throws IOException {
        List<ChampionDto> allChampions = getTFT14Champions();
        List<ChampionDto> selectedChampions = new ArrayList<>();
        Map<String, Integer> traitCounts = new HashMap<>();
        
        // Inicjalizacja licznika traitów
        for (String trait : request.getTraits()) {
            traitCounts.put(trait, 0);
        }
        
        // Sortujemy championów według liczby pożądanych traitów i kosztu
        List<ChampionDto> sortedChampions = allChampions.stream()
            .filter(champion -> champion.getTraits().stream()
                .anyMatch(request.getTraits()::contains))
            .sorted((c1, c2) -> {
                // Najpierw porównujemy liczbę pasujących traitów
                long c1Matches = c1.getTraits().stream()
                    .filter(request.getTraits()::contains)
                    .count();
                long c2Matches = c2.getTraits().stream()
                    .filter(request.getTraits()::contains)
                    .count();
                
                int traitComparison = Long.compare(c2Matches, c1Matches);
                if (traitComparison != 0) {
                    return traitComparison;
                }
                
                // Jeśli liczba traitów jest taka sama, wybieramy tańszego championa
                return Integer.compare(c1.getCost(), c2.getCost());
            })
            .collect(Collectors.toList());
        
        // Wybieramy championów z największą liczbą pasujących traitów
        for (ChampionDto champion : sortedChampions) {
            // Sprawdzamy czy osiągnęliśmy maksymalny rozmiar drużyny
            if (selectedChampions.size() >= request.getTeamSize()) {
                break;
            }
            
            // Sprawdzamy czy champion ma jakiekolwiek pożądane traity
            List<String> matchingTraits = champion.getTraits().stream()
                .filter(request.getTraits()::contains)
                .collect(Collectors.toList());
                
            if (!matchingTraits.isEmpty()) {
                // Dodajemy championa tylko jeśli pomoże osiągnąć próg dla jakiegoś traitu
                boolean willHelpAchieveTrait = matchingTraits.stream()
                    .anyMatch(trait -> traitCounts.get(trait) < 3);
                    
                if (willHelpAchieveTrait) {
                    selectedChampions.add(champion);
                    // Aktualizujemy liczniki traitów
                    for (String trait : matchingTraits) {
                        traitCounts.put(trait, traitCounts.get(trait) + 1);
                    }
                }
            }
        }
        
        // Zbieramy aktywne traity (te, które mają co najmniej 3 championów)
        Set<String> activeTraits = traitCounts.entrySet().stream()
            .filter(entry -> entry.getValue() >= 3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        
        return TeamCompositionDto.builder()
            .champions(selectedChampions)
            .traitCounts(traitCounts)
            .activeTraits(activeTraits)
            .build();
    }
}
