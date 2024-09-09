package com.intheeast.springframe.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.intheeast.springframe.domain.Level;
import com.intheeast.springframe.domain.User;
import com.intheeast.springframe.sqlservice.SqlService;

@Repository("userDao")
public class UserDaoJdbc implements UserDao {
	
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private SqlService sqlService;
	
	@Autowired
	@Qualifier("jdbcDataSource")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Autowired
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}
	
	private RowMapper<User> userRowMapper() {
		return (rs, rowNum) -> {
			User user = new User();
			user.setId(rs.getString("id"));
			user.setName(rs.getString("name"));
			user.setPassword(rs.getString("password"));
			user.setEmail(rs.getString("email"));
			user.setLevel(Level.valueOf(rs.getInt("level")));
			user.setLogin(rs.getInt("login"));
			user.setRecommend(rs.getInt("recommend"));
			return user;
		};
	}
	

	// A aspect가 있는데,
	// add 메서드-조인 포인트-
	// 지원하는 방식이...
	// A aspect를 add에 지원하는데,,,
	@Override
	public void add(User user) {
		try {
			this.jdbcTemplate.update(
					this.sqlService.getSql("userAdd"),
							user.getId(), 
							user.getName(), 
							user.getPassword(), 
							user.getEmail(),
							user.getLevel().intValue(), 
							user.getLogin(), 
							user.getRecommend());
		} catch (DataAccessException de) {
			System.out.println(de);			
		} 

	}

	@Override
	public Optional<User> get(String id) {
		
		try (Stream<User> stream = 
				jdbcTemplate.queryForStream(
						this.sqlService.getSql("userGet"), 
						userRowMapper(),
						id)) {
			return stream.findFirst();
		} catch (DataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<User> getAll() {
		return this.jdbcTemplate.query(
				this.sqlService.getSql("userGetAll"),
				userRowMapper());

	}

	@Override
	public void deleteAll() {
		this.jdbcTemplate.update(this.sqlService.getSql("userDeleteAll"));
	}

	@Override
	public int getCount() {
		List<Integer> result = jdbcTemplate.query(
				this.sqlService.getSql("userGetCount"),
				(rs, rowNum) -> rs.getInt(1));
		
		return DataAccessUtils.singleResult(result);
	}
	
	@Override
	public void update(User user) {
		this.jdbcTemplate.update(
				this.sqlService.getSql("userUpdate"),
				user.getName(), 
				user.getPassword(), 
				user.getEmail(),
				user.getLevel().intValue(), ///////
				user.getLogin(), 
				user.getRecommend(),
				user.getId());
		
	}

}
