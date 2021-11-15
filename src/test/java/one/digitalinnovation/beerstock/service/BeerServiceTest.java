package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.dto.QuantityDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.*;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;
//o
    @Test
    void beerCreated() throws BeerAlreadyRegisteredException, BeerStockRequiredFieldException {
        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        //then
        BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);
        assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
        assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
        assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));
    }
//o
    @Test
    void alreadyRegisteredBeerException() {
        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));

        // then
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
    }
//o
    @Test
    void validBeerNameReturnABeer() throws BeerNotFoundException {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        // when
        when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));

        // then
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
    }
//o
    @Test
    void notRegisteredBeerNameException() {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

        // then
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
    }
//o
    @Test
    void listBeerIsCalledThenReturnAListOfBeers() {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //when
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));

        //then
        List<BeerDTO> foundListBeersDTO = beerService.listAll();
        assertThat(foundListBeersDTO, is(not(empty())));
        assertThat(foundListBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
    }
//o
    @Test
    void listBeerReturnAnEmptyListOfBeers() {
        //when
        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        //then
        List<BeerDTO> foundListBeersDTO = beerService.listAll();
        assertThat(foundListBeersDTO, is(empty()));
    }
//o
    @Test
    void exclusionValidIdBeerDeleted() throws BeerNotFoundException{
        // given
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);
        // when
        when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
        doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());
        // then
        beerService.deleteById(expectedDeletedBeerDTO.getId());
        verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
    }

//o
    @Test
    void incrementBeerStock() throws BeerNotFoundException, BeerStockExceededException, NegativeInputException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // then
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
    }
//o
    @Test
    void incrementBeforeSumGreaterThanMaxException() {

        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        int quantityToIncrement = 80;

        //then
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }
//o
    @Test
    void incrementAfterSumGreaterThanMaxException() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        //then
        int quantityToIncrement = 45;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }
//o
    @Test
    void incrementInvalidIdException() {
        //given
        int quantityToIncrement = 10;
        //when
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
        //then
        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }
//i
    @Test
    void decrementBeerStock() throws BeerNotFoundException, BeerStockExceededException, BeerStockMinCapacityExceededException, NegativeInputException {

        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
        int quantityToDecrement = 5;
        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;

        //then
        BeerDTO decrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);
        assertThat(expectedQuantityAfterDecrement, equalTo(decrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterDecrement, greaterThan(-1));
    }
    //i
    @Test
    void decrementEmptyBeerStock() throws BeerNotFoundException {

        //given
        QuantityDTO expectedStockDTO = QuantityDTO.builder()
                .quantity(10)
                .build();
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
        int quantityToDecrement = 80;

        //then
        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
    }
//i
    @Test
    void decrementAfterSumLowerThanZeroException() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        int quantityToDecrement = 80;
        //then
        assertThrows(BeerStockMinCapacityExceededException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
    }

//i
    @Test
    void decrementInvalidIdException() {
        //given
        int quantityToDecrement = 10;
        //when
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
        //then
        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
    }

    //new
    @Test
    void exclusionInvalidIdException() {
        //when
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());
        //then
        assertThrows(BeerNotFoundException.class, () -> beerService.deleteById(INVALID_BEER_ID));
    }

    //new
    @Test
    void decrementBeforeSumLowerThanZeroException() {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
        //when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        int quantityToDecrement = -1;
        //then
        assertThrows(NegativeInputException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
    }

}
