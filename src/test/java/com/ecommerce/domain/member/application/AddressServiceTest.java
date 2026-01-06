package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.dao.AddressRepository;
import com.ecommerce.domain.member.domain.Address;
import com.ecommerce.domain.member.dto.AddressRequest;
import com.ecommerce.domain.member.dto.AddressResponse;
import com.ecommerce.domain.member.exception.MemberException;
import com.ecommerce.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @InjectMocks
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Nested
    @DisplayName("배송지 목록 조회")
    class GetAddressesTest {

        @Test
        @DisplayName("회원의 배송지 목록을 조회한다")
        void getAddresses_Success() {
            // given
            Address address1 = createAddress(1L, 1L, "집", true);
            Address address2 = createAddress(2L, 1L, "회사", false);

            given(addressRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(1L))
                    .willReturn(List.of(address1, address2));

            // when
            List<AddressResponse> responses = addressService.getAddresses(1L);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo("집");
            assertThat(responses.get(0).isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("배송지 생성")
    class CreateAddressTest {

        @Test
        @DisplayName("첫 번째 배송지는 자동으로 기본 배송지가 된다")
        void createAddress_FirstAddress_BecomesDefault() {
            // given
            AddressRequest request = createAddressRequest("집", false);
            given(addressRepository.countByMemberId(1L)).willReturn(0);

            Address savedAddress = createAddress(1L, 1L, "집", true);
            given(addressRepository.save(any(Address.class))).willReturn(savedAddress);

            // when
            AddressResponse response = addressService.createAddress(1L, request);

            // then
            assertThat(response.isDefault()).isTrue();
        }

        @Test
        @DisplayName("배송지가 5개를 초과하면 예외가 발생한다")
        void createAddress_ExceedsLimit_ThrowsException() {
            // given
            AddressRequest request = createAddressRequest("새 배송지", false);
            given(addressRepository.countByMemberId(1L)).willReturn(5);

            // when & then
            assertThatThrownBy(() -> addressService.createAddress(1L, request))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
                    });
        }

        @Test
        @DisplayName("기본 배송지로 설정하면 기존 기본 배송지가 해제된다")
        void createAddress_SetAsDefault_ClearsPreviousDefault() {
            // given
            AddressRequest request = createAddressRequest("새 배송지", true);
            given(addressRepository.countByMemberId(1L)).willReturn(1);

            Address savedAddress = createAddress(1L, 1L, "새 배송지", true);
            given(addressRepository.save(any(Address.class))).willReturn(savedAddress);

            // when
            addressService.createAddress(1L, request);

            // then
            verify(addressRepository).clearDefaultAddress(1L);
        }
    }

    @Nested
    @DisplayName("배송지 수정")
    class UpdateAddressTest {

        @Test
        @DisplayName("배송지 정보를 수정한다")
        void updateAddress_Success() {
            // given
            Address address = createAddress(1L, 1L, "집", false);
            given(addressRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(address));

            AddressRequest request = createAddressRequest("회사", false);

            // when
            AddressResponse response = addressService.updateAddress(1L, 1L, request);

            // then
            assertThat(response.getName()).isEqualTo("회사");
        }

        @Test
        @DisplayName("존재하지 않는 배송지 수정 시 예외가 발생한다")
        void updateAddress_NotFound_ThrowsException() {
            // given
            given(addressRepository.findByIdAndMemberId(anyLong(), anyLong())).willReturn(Optional.empty());

            AddressRequest request = createAddressRequest("회사", false);

            // when & then
            assertThatThrownBy(() -> addressService.updateAddress(1L, 999L, request))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("배송지 삭제")
    class DeleteAddressTest {

        @Test
        @DisplayName("배송지를 삭제한다")
        void deleteAddress_Success() {
            // given
            Address address = createAddress(1L, 1L, "회사", false);
            given(addressRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(address));

            // when
            addressService.deleteAddress(1L, 1L);

            // then
            verify(addressRepository).delete(address);
        }

        @Test
        @DisplayName("기본 배송지 삭제 시 예외가 발생한다")
        void deleteAddress_DefaultAddress_ThrowsException() {
            // given
            Address address = createAddress(1L, 1L, "집", true);
            given(addressRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(address));

            // when & then
            assertThatThrownBy(() -> addressService.deleteAddress(1L, 1L))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS);
                    });
        }
    }

    @Nested
    @DisplayName("기본 배송지 설정")
    class SetDefaultAddressTest {

        @Test
        @DisplayName("배송지를 기본 배송지로 설정한다")
        void setDefaultAddress_Success() {
            // given
            Address address = createAddress(1L, 1L, "회사", false);
            given(addressRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(address));

            // when
            addressService.setDefaultAddress(1L, 1L);

            // then
            verify(addressRepository).clearDefaultAddress(1L);
            assertThat(address.isDefault()).isTrue();
        }
    }

    private AddressRequest createAddressRequest(String name, boolean isDefault) {
        AddressRequest request = new AddressRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "recipientName", "홍길동");
        ReflectionTestUtils.setField(request, "phoneNumber", "010-1234-5678");
        ReflectionTestUtils.setField(request, "zipCode", "12345");
        ReflectionTestUtils.setField(request, "address1", "서울시 강남구");
        ReflectionTestUtils.setField(request, "address2", "101동 1001호");
        ReflectionTestUtils.setField(request, "isDefault", isDefault);
        return request;
    }

    private Address createAddress(Long id, Long memberId, String name, boolean isDefault) {
        Address address = Address.builder()
                .memberId(memberId)
                .name(name)
                .recipientName("홍길동")
                .phoneNumber("010-1234-5678")
                .zipCode("12345")
                .address1("서울시 강남구")
                .address2("101동 1001호")
                .isDefault(isDefault)
                .build();
        ReflectionTestUtils.setField(address, "id", id);
        ReflectionTestUtils.setField(address, "createdAt", LocalDateTime.now());
        return address;
    }
}
