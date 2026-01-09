package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.dao.AddressRepository;
import com.ecommerce.domain.member.domain.Address;
import com.ecommerce.domain.member.dto.AddressRequest;
import com.ecommerce.domain.member.dto.AddressResponse;
import com.ecommerce.domain.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private static final int MAX_ADDRESS_COUNT = 5;

    private final AddressRepository addressRepository;

    public List<AddressResponse> getAddresses(Long memberId) {
        return addressRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(memberId)
                .stream()
                .map(AddressResponse::from)
                .toList();
    }

    public AddressResponse getAddress(Long memberId, Long addressId) {
        Address address = findAddressByIdAndMemberId(addressId, memberId);
        return AddressResponse.from(address);
    }

    @Transactional
    public AddressResponse createAddress(Long memberId, AddressRequest request) {
        int currentCount = addressRepository.countByMemberId(memberId);
        if (currentCount >= MAX_ADDRESS_COUNT) {
            throw MemberException.addressLimitExceeded();
        }

        if (request.isDefault()) {
            addressRepository.clearDefaultAddress(memberId);
        }

        boolean shouldBeDefault = request.isDefault() || currentCount == 0;

        Address address = Address.builder()
                .memberId(memberId)
                .name(request.getName())
                .recipientName(request.getRecipientName())
                .phoneNumber(request.getPhoneNumber())
                .zipCode(request.getZipCode())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .isDefault(shouldBeDefault)
                .build();

        Address savedAddress = addressRepository.save(address);
        return AddressResponse.from(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(Long memberId, Long addressId, AddressRequest request) {
        Address address = findAddressByIdAndMemberId(addressId, memberId);

        if (request.isDefault() && !address.isDefault()) {
            addressRepository.clearDefaultAddress(memberId);
            address.setAsDefault();
        }

        address.update(
                request.getName(),
                request.getRecipientName(),
                request.getPhoneNumber(),
                request.getZipCode(),
                request.getAddress1(),
                request.getAddress2()
        );

        return AddressResponse.from(address);
    }

    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {
        Address address = findAddressByIdAndMemberId(addressId, memberId);

        if (address.isDefault()) {
            throw MemberException.cannotDeleteDefaultAddress();
        }

        addressRepository.delete(address);
    }

    @Transactional
    public void setDefaultAddress(Long memberId, Long addressId) {
        Address address = findAddressByIdAndMemberId(addressId, memberId);

        if (!address.isDefault()) {
            addressRepository.clearDefaultAddress(memberId);
            address.setAsDefault();
        }
    }

    private Address findAddressByIdAndMemberId(Long addressId, Long memberId) {
        return addressRepository.findByIdAndMemberId(addressId, memberId)
                .orElseThrow(MemberException::addressNotFound);
    }
}
