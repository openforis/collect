package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcUser.OFC_USER;
import static org.openforis.collect.persistence.jooq.tables.OfcUserUsergroup.OFC_USER_USERGROUP;
import static org.openforis.collect.persistence.jooq.tables.OfcUsergroup.OFC_USERGROUP;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordHandler;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.UserGroupRole;
import org.openforis.collect.model.UserGroup.UserGrupJoinRequestStatus;
import org.openforis.collect.model.UserGroup.UserInGroup;
import org.openforis.collect.model.UserGroup.Visibility;
import org.openforis.collect.persistence.jooq.Sequences;
import org.openforis.collect.persistence.jooq.tables.daos.OfcUsergroupDao;

public class UserGroupDao extends OfcUsergroupDao implements PersistedObjectDao<UserGroup, Integer> {

	public UserGroupDao(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	public void update(UserGroup item) {
		super.update(item);
	}

	@Override
	public void delete(Integer id) {
		super.deleteById(id);
	}

	@Override
	public List<UserGroup> loadAll() {
		return dsl()
			.selectFrom(OFC_USERGROUP)
			.orderBy(OFC_USERGROUP.NAME)
			.fetchInto(UserGroup.class);
	}

	@Override
	public UserGroup loadById(Integer id) {
		return dsl()
			.selectFrom(OFC_USERGROUP)
			.where(OFC_USERGROUP.ID.eq(id))
			.fetchOneInto(UserGroup.class);
	}
	
	@Override
	public void insert(UserGroup userGroup) {
		DSLContext dsl = dsl();
		int id;
		if (dsl.dialect() == SQLDialect.SQLITE) {
			int maxId = (Integer) dsl.select(DSL.max(OFC_USERGROUP.ID)).from(OFC_USERGROUP).fetchOne().getValue(0);
			id = maxId + 1;
		} else {
			id = dsl.nextval(Sequences.OFC_USERGROUP_ID_SEQ).intValue();
		}
		userGroup.setId(id);
		super.insert(userGroup);
	}
	
	public void save(UserGroup userGroup) {
		if (userGroup.getId() == null) {
			insert(userGroup);
		} else {
			update(userGroup);
		}
	}
	
	public void insertRelation(User user, UserGroup group, UserGroupRole role, UserGrupJoinRequestStatus joinStatus, Date memberSince) {
		dsl().insertInto(OFC_USER_USERGROUP, OFC_USER_USERGROUP.GROUP_ID, OFC_USER_USERGROUP.USER_ID, 
				OFC_USER_USERGROUP.REQUEST_DATE, OFC_USER_USERGROUP.MEMBER_SINCE, 
				OFC_USER_USERGROUP.STATUS_CODE, OFC_USER_USERGROUP.ROLE_CODE)
			.values(group.getId(), user.getId(), new Timestamp(System.currentTimeMillis()),
					memberSince == null ? null : new Timestamp(memberSince.getTime()), String.valueOf(joinStatus.getCode()),
					String.valueOf(role.getCode()))
			.execute();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<UserInGroup> findUsersByGroup(UserGroup userGroup) {
		final List<UserInGroup> result = new ArrayList<UserInGroup>();
		
		dsl().select(OFC_USER_USERGROUP.USER_ID, OFC_USER_USERGROUP.ROLE_CODE, 
				OFC_USER.USERNAME, OFC_USER.ENABLED)
			.from(OFC_USER_USERGROUP).join(OFC_USER).on(OFC_USER.ID.eq(OFC_USER_USERGROUP.USER_ID))
			.where(OFC_USER_USERGROUP.GROUP_ID.eq(userGroup.getId())
					.and(OFC_USER_USERGROUP.STATUS_CODE.eq(String.valueOf(UserGroup.UserGrupJoinRequestStatus.ACCEPTED.getCode()))))
			.orderBy(OFC_USER.USERNAME)
			.fetchInto(new RecordHandler() {
				public void next(Record record) {
					User user = new User();
					user.setId(record.getValue(OFC_USER_USERGROUP.USER_ID));
					user.setUsername(record.getValue(OFC_USER.USERNAME));
					user.setEnabled(record.getValue(OFC_USER.ENABLED).equalsIgnoreCase("Y"));
					UserGroupRole role = UserGroupRole.fromCode(record.getValue(OFC_USER_USERGROUP.ROLE_CODE));
					result.add(new UserInGroup(user, role));
				}
			});
		return result;
	}
	
	public List<UserGroup> findByUser(User user) {
		DSLContext dsl = dsl();
		List<UserGroup> result = dsl.selectFrom(OFC_USERGROUP)
			.where(
				OFC_USERGROUP.ID.in(
					dsl.select(OFC_USER_USERGROUP.GROUP_ID)
						.from(OFC_USER_USERGROUP)
						.where(OFC_USER_USERGROUP.USER_ID.eq(user.getId())
							.and(
								OFC_USER_USERGROUP.STATUS_CODE.eq(String.valueOf(UserGrupJoinRequestStatus.ACCEPTED.getCode()))
							)
						)
					)
				)
			.orderBy(OFC_USERGROUP.LABEL)
			.fetchInto(UserGroup.class);
		return result;
	}

	public List<UserGroup> findPublicGroups() {
		return dsl()
				.selectFrom(OFC_USERGROUP)
				.where(OFC_USERGROUP.VISIBILITY_CODE.eq(String.valueOf(Visibility.PUBLIC.getCode())))
				.fetchInto(UserGroup.class);
	}
	
	public List<UserGroup> findPublicUserDefinedGroups() {
		return dsl()
				.selectFrom(OFC_USERGROUP)
				.where(OFC_USERGROUP.SYSTEM_DEFINED.eq(false)
					.and(OFC_USERGROUP.VISIBILITY_CODE.eq(String.valueOf(Visibility.PUBLIC.getCode()))))
				.fetchInto(UserGroup.class);
	}

	public void acceptJoinRequest(User user, UserGroup userGroup) {
		dsl().update(OFC_USER_USERGROUP)
			.set(OFC_USER_USERGROUP.STATUS_CODE, String.valueOf(UserGrupJoinRequestStatus.ACCEPTED.getCode()))
			.where(OFC_USER_USERGROUP.GROUP_ID.eq(userGroup.getId()).and(OFC_USER_USERGROUP.USER_ID.eq(user.getId())))
			.execute();
	}

	public UserGroup loadByName(String name) {
		return dsl()
			.selectFrom(OFC_USERGROUP)
			.where(OFC_USERGROUP.NAME.eq(name))
			.fetchOneInto(UserGroup.class);
	}
	
	private DSLContext dsl() {
		return DSL.using(configuration());
	}

}
