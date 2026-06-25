package com.nilesh.knowledgebase.mapper;

import com.nilesh.knowledgebase.dto.TagDto;
import com.nilesh.knowledgebase.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagDto toDto(Tag tag) {
        if (tag == null) {
            return null;
        }
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
    
    public Tag toEntity(TagDto tagDto) {
        if (tagDto == null) {
            return null;
        }
        return Tag.builder()
                .id(tagDto.getId())
                .name(tagDto.getName())
                .color(tagDto.getColor())
                .build();
    }
}
