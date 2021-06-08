package kr.re.keti.repository;

import kr.re.keti.service.M2MRetrofit;
import kr.re.keti.service.M2MService;

public class M2MRepository {
    private final M2MService m2MService;

    public M2MRepository() {
        m2MService = M2MRetrofit.getInstance().create(M2MService.class);
    }


}
