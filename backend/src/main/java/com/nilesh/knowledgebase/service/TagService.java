package com.nilesh.knowledgebase.service;

import com.nilesh.knowledgebase.dto.TagDto;
import com.nilesh.knowledgebase.entity.Tag;
import com.nilesh.knowledgebase.exception.ResourceNotFoundException;
import com.nilesh.knowledgebase.mapper.TagMapper;
import com.nilesh.knowledgebase.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Transactional(readOnly = true)
    public List<TagDto> getAllTags() {
        return tagRepository.findAllByOrderByNameAsc().stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TagDto getTagById(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        return tagMapper.toDto(tag);
    }

    @Transactional
    public TagDto createTag(TagDto tagDto) {
        if (tagRepository.findByName(tagDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Tag with this name already exists");
        }
        Tag tag = new Tag();
        tag.setName(tagDto.getName());
        tag.setColor(tagDto.getColor());
        return tagMapper.toDto(tagRepository.save(tag));
    }

    @Transactional
    public TagDto updateTag(UUID id, TagDto tagDto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        
        tag.setName(tagDto.getName());
        tag.setColor(tagDto.getColor());
        return tagMapper.toDto(tagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(UUID id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag not found with id: " + id);
        }
        tagRepository.deleteById(id);
    }
}
