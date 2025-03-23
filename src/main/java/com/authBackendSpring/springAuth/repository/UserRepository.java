package com.authBackendSpring.springAuth.repository;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;   
import com.authBackendSpring.springAuth.models.Users;

@Repository
public interface UserRepository extends MongoRepository<Users,String>{

    Optional<Users> findByEmail(String email);

    public Users findByName(String name);

    public Users deleteByEmail(String email);

    @Query("{ '_id': ?0 }")
    Optional<Users> findByObjectId(ObjectId id);
    
    //custom query to find by email and update pass. only
    @Query("{'email':?0}")
    @Update("{'$set':{'password':?1}}")
    void updatePasswordByEmail(String email,String password);
}
