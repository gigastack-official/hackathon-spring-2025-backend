package ru.gigastack.digitalmine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RFIDDto {
    @NotBlank(message = "ID метки не может быть пустым")
    private String tagId;

    // Необязательное поле, указывающее действие, например "enter" или "exit"
    private String action;
}