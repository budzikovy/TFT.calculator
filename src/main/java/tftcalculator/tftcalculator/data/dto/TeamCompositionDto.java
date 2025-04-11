package tftcalculator.tftcalculator.data.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
@ToString
public class TeamCompositionDto {
    private List<ChampionDto> champions;
    private Map<String, Integer> traitCounts;
    private Set<String> activeTraits;
}