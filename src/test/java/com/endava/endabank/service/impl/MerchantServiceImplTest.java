package com.endava.endabank.service.impl;

import com.endava.endabank.constants.MerchantStates;
import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.MerchantDao;
import com.endava.endabank.dto.merchant.MerchantRegisterDto;
import com.endava.endabank.exceptions.custom.UniqueConstraintViolationException;
import com.endava.endabank.model.Merchant;
import com.endava.endabank.model.User;
import com.endava.endabank.service.MerchantRequestStateService;
import com.endava.endabank.service.UserService;
import com.endava.endabank.utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;


import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MerchantServiceImplTest {

    @Mock
    private MerchantDao merchantDao;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MerchantRequestStateService merchantRequestStateService;

    @Mock
    private UserService userService;
    @InjectMocks
    private MerchantServiceImpl merchantService;

    @BeforeEach
    void setUp(){
        merchantService = new MerchantServiceImpl(merchantDao,modelMapper,merchantRequestStateService,userService);
    }

    @Test
    void testFindByIdShouldSuccessWhenDataCorrect(){
        Merchant merchantNotReviewed = TestUtils.getMerchantNotReviewed();

        when(merchantDao.findById(1)).thenReturn(Optional.of(merchantNotReviewed));

        Merchant merchant = merchantService.findById(1);

        assertEquals(merchantNotReviewed.getId(), merchant.getId());
        assertEquals(merchantNotReviewed.getTaxId(), merchant.getTaxId());
        assertEquals(merchantNotReviewed.getAddress(), merchant.getAddress());
        assertEquals(merchantNotReviewed.getStoreName(), merchant.getStoreName());
        assertEquals(merchantNotReviewed.getUser().getId(), merchant.getUser().getId());
        assertEquals(merchantNotReviewed.getMerchantRequestState().getId(), merchant.getMerchantRequestState().getId());

    }

    @Nested
    @DisplayName("Merchant save test")
    class SaveMerchantTests {
        MerchantRegisterDto merchantRegisterDto;

        @BeforeEach
        void setUp(){
            merchantRegisterDto = TestUtils.getMerchantRegisterDto();
            when(modelMapper.map(merchantRegisterDto, Merchant.class)).thenReturn(TestUtils.getMerchantNotReviewed());
        }

        @Test
        void testSaveShouldFailWhenUserAlreadyRegisterAMerchant(){
            User user = TestUtils.getUserNotAdmin();
            when(userService.findById(user.getId())).thenReturn(user);
            when(merchantDao.findByUser(user))
                    .thenReturn(Optional.of(TestUtils.getMerchantNotReviewed()));
            assertThrows(UniqueConstraintViolationException.class, () -> merchantService.save(user.getId(),merchantRegisterDto));
        }

        @Test
        void testSaveShouldFailWhenTaxIdAlreadyExists(){
            User user = TestUtils.getUserNotAdmin();
            when(userService.findById(user.getId())).thenReturn(user);
            when(merchantDao.findByUser(user)).thenReturn(Optional.empty());
            when(merchantDao.findByTaxId(merchantRegisterDto.getTaxId()))
                    .thenReturn(Optional.of(TestUtils.getMerchantNotReviewed()));

            assertThrows(UniqueConstraintViolationException.class, () -> merchantService.save(user.getId(),merchantRegisterDto));
        }

        @Test
        void testSaveShouldSuccessWhenDataCorrect(){
            Merchant merchant = TestUtils.getMerchantNotReviewed();
            User user = TestUtils.getUserNotAdmin();

            when(userService.findById(user.getId())).thenReturn(user);
            when(merchantDao.findByUser(user)).thenReturn(Optional.empty());
            when(merchantDao.findByTaxId(merchantRegisterDto.getTaxId())).thenReturn(Optional.empty());
            when(merchantRequestStateService.findById(MerchantStates.PENDING))
                    .thenReturn(TestUtils.getPendingMerchantRequestState());

            Map<String, String> map = merchantService.save(user.getId(), merchantRegisterDto);

            assertEquals(Strings.MERCHANT_REQUEST_CREATED,map.get(Strings.MESSAGE_RESPONSE));

        }

    }


}