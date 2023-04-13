package com.example.errorBook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.errorBook.entity.Collection;
import com.example.errorBook.mapper.CollectionMapper;
import com.example.errorBook.service.CollectionService;
import org.springframework.stereotype.Service;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, Collection> implements CollectionService {
}
