package tftcalculator.tftcalculator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import tftcalculator.tftcalculator.config.TftConstants;
import tftcalculator.tftcalculator.data.dto.ChampionDto;

@Mapper(componentModel = "spring")
public abstract class ChampionMapper {
    
    @Autowired
    protected TftConstants tftConstants;
    
    @Named("removePrefix")
    protected String removePrefix(String value) {
        if (value != null && value.startsWith(tftConstants.getTftPrefix())) {
            return value.replace(tftConstants.getTftPrefix(), "");
        }
        return value;
    }
    
    @Mapping(target = "characterName", source = "characterName", qualifiedByName = "removePrefix")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "cost", source = "cost")
    @Mapping(target = "traits", source = "traits")
    public abstract ChampionDto toDto(ChampionDto entity);
}