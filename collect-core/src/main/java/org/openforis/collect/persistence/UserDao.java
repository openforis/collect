package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.OFC_USER_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Sequences.OFC_USER_ROLE_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER_ROLE;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.DeleteQuery;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.persistence.UserDao.UserDSLContext;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRoleRecord;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class UserDao extends MappingJooqDaoSupport<Integer, User, UserDSLContext> implements PersistedObjectDao<User, Integer> {

	public UserDao() {
		super(UserDao.UserDSLContext.class);
	}
	
	public int countAll() {
		Record result = dsl().selectCount().from(OFC_USER).fetchOne();
		return (Integer) result.getValue(0);
	}

	@Override
	public User loadById(Integer id){
		UserDSLContext dsl = dsl();
		SelectQuery<?> query = dsl.selectByIdQuery(id);
		Record r = query.fetchOne();
		User user = r != null ? dsl.fromRecord(r): null;
		return user;
	}
	
	public User loadByUserName(String userName, Boolean enabled){
		UserDSLContext dsl = dsl();
		SelectConditionStep<OfcUserRecord> query = 
				dsl.selectFrom(OFC_USER)
				.where(OFC_USER.USERNAME.equal(userName));
		if ( enabled != null ) {
			String enabledFlag = enabled ? "Y": "N";
			query.and(OFC_USER.ENABLED.equal(enabledFlag));
		}
		Record r = query.fetchOne();
		User user = r != null ? dsl.fromRecord(r): null;
		return user;
	}
	
	@Override
	public List<User> loadAll() {
		UserDSLContext dsl = dsl();
		Result<OfcUserRecord> r = 
				dsl.selectFrom(OFC_USER)
				.orderBy(OFC_USER.USERNAME)
				.fetch();
		
		List<User> users = r != null ? dsl.fromResult(r): null;
		return users;
	}
	
	public int getUserId(String username) {
		UserDSLContext jf = dsl();
		Record record =
				jf.select(OFC_USER.ID)
				.from(OFC_USER)
				.where(OFC_USER.USERNAME.equal(username))
				.fetchOne();
		Integer id = record.getValue(OFC_USER.ID);
		return id;
	}

	@Override
	public void insert(User user) {
		super.insert(user);
		dsl().saveRoles(user);
	}

	@Override
	public void update(User user) {
		super.update(user);
		dsl().saveRoles(user);
	}

	@Override
	public void delete(Integer id) {
		dsl().deleteRoles(id);
		super.delete(id);
	}
	
	public static class UserDSLContext extends MappingDSLContext<Integer, User> {

		private static final long serialVersionUID = 1L;

		public UserDSLContext(Configuration config) {
			super(config, OFC_USER.ID, OFC_USER_ID_SEQ, User.class);
		}

		@Override
		protected void setId(User user, Integer id) {
			user.setId(id);
		}

		@Override
		protected Integer getId(User user) {
			return user.getId();
		}

		@Override
		protected void fromRecord(Record r, User user) {
			String enabledFlag = r.getValue(OFC_USER.ENABLED);
			boolean enabled = "Y".equals(enabledFlag);
			user.setEnabled(enabled);
			user.setId(r.getValue(OFC_USER.ID));
			user.setUsername(r.getValue(OFC_USER.USERNAME));
			user.setPassword(r.getValue(OFC_USER.PASSWORD));
			
			loadRoles(user);
		}

		@Override
		protected void fromObject(User user, StoreQuery<?> q) {
			Boolean enabled = user.getEnabled();
			String enabledFlag = enabled != null && enabled.booleanValue() ? "Y": "N";
			q.addValue(OFC_USER.ENABLED, enabledFlag);
			q.addValue(OFC_USER.ID, user.getId());
			q.addValue(OFC_USER.USERNAME, user.getUsername());
			q.addValue(OFC_USER.PASSWORD, user.getPassword());
		}
		
		protected void loadRoles(User user) {
			SelectQuery<OfcUserRoleRecord> query = selectQuery(OFC_USER_ROLE);
			query.addConditions(OFC_USER_ROLE.USER_ID.equal(user.getId()));
			Result<OfcUserRoleRecord> result = query.fetch();
			List<UserRole> roles = new ArrayList<UserRole>();
			for (OfcUserRoleRecord ofcUserRoleRecord : result) {
				String roleCode = ofcUserRoleRecord.getRole();
				UserRole role = UserRole.fromCode(roleCode);
				roles.add(role);
			}
			user.setRoles(roles);
		}
		
		protected void saveRoles(User user) {
			Integer userId = user.getId();
			deleteRoles(userId);
			List<UserRole> roles = user.getRoles();
			for (UserRole role : roles) {
				int userRoleId = nextId(OFC_USER_ROLE.ID, OFC_USER_ROLE_ID_SEQ);
				insertInto(OFC_USER_ROLE, 
							OFC_USER_ROLE.ID, 
							OFC_USER_ROLE.USER_ID, 
							OFC_USER_ROLE.ROLE)
					.values(userRoleId, userId, role.getCode())
					.execute();
			}
		}
		
		protected void deleteRoles(int userId) {
			DeleteQuery<OfcUserRoleRecord> deleteQuery = deleteQuery(OFC_USER_ROLE);
			deleteQuery.addConditions(OFC_USER_ROLE.USER_ID.equal(userId));
			deleteQuery.execute();
		}
	}
}
