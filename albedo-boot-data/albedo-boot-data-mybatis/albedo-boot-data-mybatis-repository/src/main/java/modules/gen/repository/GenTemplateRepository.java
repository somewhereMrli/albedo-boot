package modules.gen.repository;

import com.albedo.java.modules.gen.domain.GenTemplate;
import org.springframework.data.mybatis.repository.support.MybatisRepository;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface GenTemplateRepository extends MybatisRepository<GenTemplate, String> {


}