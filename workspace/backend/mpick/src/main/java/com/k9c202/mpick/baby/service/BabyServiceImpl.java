package com.k9c202.mpick.baby.service;

import com.k9c202.mpick.baby.dto.BabyDto;
import com.k9c202.mpick.baby.dto.request.BabyRequestDto;
import com.k9c202.mpick.baby.entity.Baby;
import com.k9c202.mpick.baby.repository.BabyRepository;
import com.k9c202.mpick.global.function.CommonFunction;
import com.k9c202.mpick.user.entity.User;
import com.k9c202.mpick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BabyServiceImpl implements BabyService{

    private final BabyRepository babyRepository;
    private final CommonFunction commonFunction;
    private final RedisTemplate<String, Object> redisTemplate;
    @Override
    public String add(BabyDto babyDto, String userName) {

        Baby baby = babyDto.toEntity(commonFunction.loadUser(userName));
        baby.setStatus("exist");
        babyRepository.save(baby);
        return "success";
    }

    @Override
    public String delete(Long babyId, String userName) {

        Optional<Baby> baby = babyRepository.findById(babyId);
        if(baby.isPresent() && baby.get().getUser().getLoginId().equals(userName)){
            baby.get().setStatus("delete");
            babyRepository.save(baby.get());
            return "success";
        }else{
            return "fail";
        }

    }

    @Override
    public String modify(BabyRequestDto babyRequestDto) {
        return null;
    }


}
