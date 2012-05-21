package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_USER_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER_ROLE;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SimpleSelectQuery;
import org.jooq.StoreQuery;
import org.jooq.impl.Factory;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.UserDao.JooqFactory;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRoleRecord;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author M. Togna
 * 
 */
@Transactional
public class UserDao extends MappingJooqDaoSupport<User, JooqFactory> {

	public UserDao() {
		super(UserDao.JooqFactory.class);
	}

	@Transactional
	public User loadById(int id){
		JooqFactory jf = getMappingJooqFactory();
		SimpleSelectQuery<?> query = jf.selectByIdQuery(id);
		Record r = query.fetchOne();
		if(r == null){
			return null;
		} else {
			User user = jf.fromRecord(r);
			jf.loadRoles(user);
			return user;
		}
	}
	
	@Transactional
	public User loadByUserName(String userName){
		JooqFactory jf = getMappingJooqFactory();
		SimpleSelectQuery<?> query = jf.selectByFieldQuery(OFC_USER.USERNAME, userName); 
		Record r = query.fetchOne();
		if(r == null){
			return null;
		} else {
			User user = jf.fromRecord(r);
			jf.loadRoles(user);
			return user;
		}
	}
	
	@Transactional
	public List<User> loadAll() {
		JooqFactory jf = getMappingJooqFactory();
		Result<OfcUserRecord> r = jf.selectFrom(OFC_USER).fetch();
		if (r == null) {
			return null;
		} else {
			List<User> users = jf.fromResult(r);
			for (User user : users) {
				jf.loadRoles(user);
			}
			return users;
		}
	}
	
	@Transactional
	public int getUserId(String username) {
		Factory jooqFactory = getJooqFactory();
		Record record = jooqFactory.select(OFC_USER.ID).from(OFC_USER).where(OFC_USER.USERNAME.equal(username)).fetchOne();
		Integer id = record.getValueAsInteger(OFC_USER.ID);
		return id;
	}

	public static class JooqFactory extends MappingJooqFactory<User> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection conn) {
			super(conn, OFC_USER.ID, OFC_USER_ID_SEQ, User.class);
		}

		@Override
		protected void setId(User entity, int id) {
			entity.setId(id);
		}

		@Override
		protected Integer getId(User entity) {
			return entity.getId();
		}

		@Override
		protected void fromRecord(Record r, User entity) {
			entity.setId(r.getValueAsInteger(OFC_USER.ID));
			entity.setName(r.getValueAsString(OFC_USER.USERNAME));
			entity.setPassword(r.getValueAsString(OFC_USER.PASSWORD));
		}

		@Override
		protected void fromObject(User user, StoreQuery<?> q) {
			q.addValue(OFC_USER.ID, user.getId());
			q.addValue(OFC_USER.USERNAME, user.getName());
			q.addValue(OFC_USER.PASSWORD, user.getPassword());
		}
		
		protected void loadRoles(User user) {
			SimpleSelectQuery<OfcUserRoleRecord> query = selectQuery(OFC_USER_ROLE);
			query.addConditions(OFC_USER_ROLE.USER_ID.equal(user.getId()));
			Result<OfcUserRoleRecord> result = query.fetch();
			List<String> roles = new ArrayList<String>();
			for (OfcUserRoleRecord ofcUserRoleRecord : result) {
				String role = ofcUserRoleRecord.getRole();
				roles.add(role);
			}
			user.setRoles(roles);
		}
	}
}
