package one.digitalinnovation.beerstock.exception;

import one.digitalinnovation.beerstock.dto.BeerDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockRequiredFieldException extends Exception{

    public BeerStockRequiredFieldException(BeerDTO beerDTO) {
        super(String.format("Beer Without Required Field", beerDTO));
    }
}
