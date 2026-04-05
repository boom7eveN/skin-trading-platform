package com.skinmarket.marketplace.service;

import com.skinmarket.marketplace.dto.PaginationResult;
import com.skinmarket.marketplace.repository.SkinRepository;
import com.skinmarket.marketplace.dto.CreateSkinRequest;
import com.skinmarket.marketplace.dto.SkinResponse;
import com.skinmarket.marketplace.dto.UpdateSkinRequest;
import com.skinmarket.marketplace.entity.Skin;
import com.skinmarket.marketplace.exception.BusinessLogicException;
import com.skinmarket.marketplace.exception.ErrorCode;
import com.skinmarket.marketplace.mapper.SkinMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SkinService {
    private final SkinRepository skinRepository;

    public SkinService(SkinRepository skinRepository) {
        this.skinRepository = skinRepository;
    }

    @Transactional
    public SkinResponse createSkin(CreateSkinRequest createSkinRequest) {
        skinRepository.findSkinByNameAndWeaponTypeAndRarity(
                createSkinRequest.name(),
                createSkinRequest.weaponType(),
                createSkinRequest.rarity()
        ).ifPresent(existingSkin -> {
            throw new BusinessLogicException(
                    ErrorCode.SKIN_ALREADY_EXISTS,
                    String.format("Skin already exists with name='%s', weaponType='%s', rarity='%s'",
                            createSkinRequest.name(),
                            createSkinRequest.weaponType(),
                            createSkinRequest.rarity())
            );
        });

        Skin newSkin = SkinMapper.toEntity(createSkinRequest);

        if (!skinRepository.createSkin(newSkin)) {
            throw new BusinessLogicException(
                    ErrorCode.UNEXPECTED_ERROR,
                    "Failed to create skin"
            );
        }

        return SkinMapper.toResponse(newSkin);
    }

    @Transactional
    public SkinResponse findSkinById(UUID id) {
        Skin skin = skinRepository.findSkinById(id)
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.SKIN_NOT_FOUND,
                        String.format("Skin with id %s not found", id)
                ));

        return SkinMapper.toResponse(skin);
    }

    @Transactional
    public List<SkinResponse> findAllSkins() {
        List<Skin> skins = skinRepository.findAllSkins();
        if (skins.isEmpty()) {
            return List.of();
        }
        return SkinMapper.toListResponses(skins);
    }

    @Transactional
    public SkinResponse updateSkinById(UUID id, UpdateSkinRequest request) {
        Skin existingSkin = skinRepository.findSkinById(id)
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.SKIN_NOT_FOUND,
                        String.format("Skin with id %s not found", id)
                ));

        if (!existingSkin.name().equals(request.name()) ||
                !existingSkin.weaponType().equals(request.weaponType()) ||
                !existingSkin.rarity().equals(request.rarity())) {
            skinRepository.findSkinByNameAndWeaponTypeAndRarity(
                    request.name(),
                    request.weaponType(),
                    request.rarity()
            ).ifPresent(skin -> {
                throw new BusinessLogicException(
                        ErrorCode.SKIN_ALREADY_EXISTS,
                        String.format("Skin already exists with name='%s', weaponType='%s', rarity='%s'",
                                request.name(),
                                request.weaponType(),
                                request.rarity())
                );
            });
        }

        Skin updatedSkin = SkinMapper.toEntity(id, request, existingSkin.version());

        if (!skinRepository.updateSkinWithOptimisticLock(updatedSkin)) {
            throw new BusinessLogicException(
                    ErrorCode.OPTIMISTIC_LOCK_FAILURE,
                    "Skin was modified by another administrator. Please refresh and try again."
            );
        }

        Skin newSkin = skinRepository.findSkinById(id).get();
        return SkinMapper.toResponse(newSkin);
    }

    @Transactional
    public void deleteSkinById(UUID id) {
        if (skinRepository.findSkinById(id).isEmpty()) {
            throw new BusinessLogicException(
                    ErrorCode.SKIN_NOT_FOUND,
                    String.format("Skin with id %s not found for deletion", id)
            );
        }

        if (!skinRepository.deleteSkinById(id)) {
            throw new BusinessLogicException(
                    ErrorCode.UNEXPECTED_ERROR,
                    String.format("Failed to delete skin with id %s", id)
            );
        }
    }

    @Transactional
    public PaginationResult<SkinResponse> findAllSkinsWithPagination(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 50) size = 50;

        PaginationResult<Skin> paginationResult = skinRepository.findAllSkinsWithPagination(page, size);

        List<SkinResponse> responses = SkinMapper.toListResponses(paginationResult.items());

        return new PaginationResult<>(
                responses,
                paginationResult.page(),
                paginationResult.size(),
                paginationResult.totalElements()
        );
    }
}
