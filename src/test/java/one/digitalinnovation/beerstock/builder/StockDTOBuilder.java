package one.digitalinnovation.beerstock.builder;
import lombok.Builder;
import one.digitalinnovation.beerstock.dto.StockDTO;

@Builder
public class StockDTOBuilder {

    @Builder.Default
    private Long id = 1L;

    @Builder.Default
    private int quantity = 0;

    public StockDTO toStockDTO() {
        return new StockDTO(id,
                quantity);
    }
}


