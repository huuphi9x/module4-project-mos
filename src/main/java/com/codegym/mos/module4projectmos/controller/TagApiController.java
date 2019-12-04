package com.codegym.mos.module4projectmos.controller;

import com.codegym.mos.module4projectmos.model.entity.Tag;
import com.codegym.mos.module4projectmos.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/tag")
public class TagApiController {
    @Autowired
    TagService tagService;

    @PostMapping(params = "action=create")
    public ResponseEntity<String> createTag(@Valid @RequestBody Tag tag) {
        Tag checkedTag = tagService.findByName(tag.getName());
        if (checkedTag != null) {
            return new ResponseEntity<>("Tag title has already existed in database!", HttpStatus.UNPROCESSABLE_ENTITY);
        } else {
            tagService.save(tag);
            return new ResponseEntity<>("Tag title created in database!", HttpStatus.CREATED);
        }
    }

    @GetMapping(params = "action=list")
    public ResponseEntity<Page<Tag>> tagList(Pageable pageable) {
        Page<Tag> tagList = tagService.findAll(pageable);
        boolean isEmpty = tagList.getTotalElements() == 0;
        if (isEmpty) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else return new ResponseEntity<>(tagList, HttpStatus.OK);
    }

    @GetMapping(params = "action=search")
    public ResponseEntity<Page<Tag>> tagSearch(@RequestParam String name, Pageable pageable) {
        Page<Tag> filteredTagList = tagService.findAllByNameContaining(name, pageable);
        boolean isEmpty = filteredTagList.getTotalElements() == 0;
        if (isEmpty) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else return new ResponseEntity<>(filteredTagList, HttpStatus.OK);
    }

    @DeleteMapping(params = {"action=delete", "id"})
    public ResponseEntity<String> deleteTag(@RequestParam Long id) {
        tagService.deleteById(id);
        return new ResponseEntity<>("Tag title removed in database!", HttpStatus.OK);
    }
}
