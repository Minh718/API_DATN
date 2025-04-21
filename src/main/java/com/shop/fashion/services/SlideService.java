package com.shop.fashion.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.SlideAddDTO;
import com.shop.fashion.entities.Slide;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.SlideRepository;
import com.shop.fashion.utils.FileUploadUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlideService {
    private final SlideRepository slideRepository;

    public Slide addSlide(SlideAddDTO slideDTO) {
        String nameImage = FileUploadUtil.saveImage(slideDTO.file());

        return slideRepository.save(
                Slide.builder().image(nameImage).description(slideDTO.description()).status(slideDTO.status())
                        .title(slideDTO.title()).build());
    }

    public Page<Slide> getAllSlides(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Slide> slides = slideRepository.findAll(pageable);
        return slides;
    }

    public List<Slide> getAllPublicSlides() {
        List<Slide> slides = slideRepository.findAllByStatus(true);
        return slides;
    }

    public void deleteSlide(Long id) {
        slideRepository.deleteById(id);
    }

    public Void activeSlide(Long id) {
        Slide slide = slideRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SLIDE_NOT_FOUND));
        slide.setStatus(true);
        slideRepository.save(slide);
        return null;
    }

    public Void unactiveSlide(Long id) {
        Slide slide = slideRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SLIDE_NOT_FOUND));
        slide.setStatus(false);
        slideRepository.save(slide);
        return null;

    }
}