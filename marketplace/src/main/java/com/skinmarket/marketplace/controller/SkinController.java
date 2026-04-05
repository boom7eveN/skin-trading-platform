package com.skinmarket.marketplace.controller;

import com.skinmarket.marketplace.dto.CreateSkinRequest;
import com.skinmarket.marketplace.dto.SkinResponse;
import com.skinmarket.marketplace.dto.UpdateSkinRequest;
import com.skinmarket.marketplace.service.SkinService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/skin")
public class SkinController {
    private final SkinService skinService;

    public SkinController(SkinService skinService) {
        this.skinService = skinService;
    }

    @GetMapping("/{id}")
    public SkinResponse getSkinById(@PathVariable UUID id) {
        return skinService.findSkinById(id);
    }

    @GetMapping
    public List<SkinResponse> getSkins() {
        return skinService.findAllSkins();
    }

    @PostMapping
    public SkinResponse createSkin(@RequestBody CreateSkinRequest createSkinRequest) {
        return skinService.createSkin(createSkinRequest);
    }

    @PutMapping("/{id}")
    public SkinResponse updateSkin(@PathVariable UUID id,
                                   @RequestBody UpdateSkinRequest updateSkinRequest) {
        return skinService.updateSkinById(id, updateSkinRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteSkinById(@PathVariable UUID id) {
        skinService.deleteSkinById(id);
    }
}


