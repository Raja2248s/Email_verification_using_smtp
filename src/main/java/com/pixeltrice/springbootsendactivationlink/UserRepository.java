package com.pixeltrice.springbootsendactivationlink;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    UserEntity findByEmailIdIgnoreCase(String emailId);

	

}
