package tftcalculator.tftcalculator.data.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamCompositionRequestDto {
    @NotEmpty(message = "Lista traitów nie może być pusta")
    private List<String> traits;
    
    @NotNull(message = "Wielkość drużyny jest wymagana")
    @Min(value = 6, message = "Minimalna wielkość drużyny to 6")
    @Max(value = 10, message = "Maksymalna wielkość drużyny to 10")
    private Integer teamSize;
}