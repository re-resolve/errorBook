package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.entity.Collection;
import com.errorbook.errorbookv1.mapper.CollectionMapper;
import com.errorbook.errorbookv1.service.CollectionService;
import org.springframework.stereotype.Service;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, Collection> implements CollectionService {
}
