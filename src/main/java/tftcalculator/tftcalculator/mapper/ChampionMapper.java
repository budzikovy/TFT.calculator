package tftcalculator.tftcalculator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tftcalculator.tftcalculator.data.dto.ChampionDto;

@Mapper(componentModel = "spring")
public interface ChampionMapper {
    
    @Named("removePrefix")
    default String removePrefix(String value) {
        if (value != null && value.startsWith("TFT14_")) {
            return value.replace("TFT14_", "");
        }
        return value;
    }
    
    @Mapping(target = "characterName", source = "characterName", qualifiedByName = "removePrefix")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "cost", source = "cost")
    @Mapping(target = "traits", source = "traits")
    ChampionDto toDto(ChampionDto entity);
}
