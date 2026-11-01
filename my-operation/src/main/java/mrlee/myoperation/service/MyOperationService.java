package mrlee.myoperation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mrlee.myoperation.domain.MyOperation;
import mrlee.myoperation.repository.MyOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class MyOperationService {

    private final MyOperationRepository myOperationRepository;

    public MyOperation myOperation(String name, int price) {
        log.info("MyOperationService.myOperation...");
        MyOperation myOperation = new MyOperation(name, price);
        return myOperationRepository.save(myOperation);
    }
}
