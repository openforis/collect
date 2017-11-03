package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcUser.OFC_USER;
import static org.openforis.collect.persistence.jooq.tables.OfcUserUsergroup.OFC_USER_USERGROUP;
import static org.openforis.collect.persistence.jooq.tables.OfcUsergroup.OFC_USERGROUP;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordHandler;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.Visibility;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupJoinRequestStatus;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.persistence.jooq.Sequences;
import org.openforis.collect.persistence.jooq.tables.daos.OfcUsergroupDao;
import org.openforis.collect.persistence.jooq.tables.records.OfcUsergroupRecord;
import org.openforis.collect.persistence.utils.Daos;

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
			.orderBy(OFC_USERGROUP.PARENT_ID, OFC_USERGROUP.LABEL)
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
	public void insert(UserGroup ug) {
		DSLContext dsl = dsl();
		if (dsl.dialect() == SQLDialect.SQLITE) {
			OfcUsergroupRecord insertResult = dsl.insertInto(OFC_USERGROUP)
				.columns(OFC_USERGROUP.CREATED_BY,
						OFC_USERGROUP.CREATION_DATE,
						OFC_USERGROUP.DESCRIPTION,
						OFC_USERGROUP.ENABLED,
						OFC_USERGROUP.LABEL,
						OFC_USERGROUP.NAME,
						OFC_USERGROUP.PARENT_ID,
						OFC_USERGROUP.QUALIFIER1_NAME,
						OFC_USERGROUP.QUALIFIER1_VALUE,
						OFC_USERGROUP.SYSTEM_DEFINED,
						OFC_USERGROUP.VISIBILITY_CODE)
				.values(ug.getCreatedBy(),
						ug.getCreationDate(),
						ug.getDescription(),
						ug.getEnabled(),
						ug.getLabel(),
						ug.getName(),
						ug.getParentId(),
						ug.getQualifier1Name(),
						ug.getQualifier1Value(),
						ug.getSystemDefined(),
						ug.getVisibilityCode())
				.returning(OFC_USERGROUP.ID)
				.fetchOne();
			ug.setId(insertResult.getId());
		} else {
			Integer id;
			id = dsl.nextval(Sequences.OFC_USERGROUP_ID_SEQ).intValue();
			ug.setId(id);
			super.insert(ug);
		}
	}
	
	public void save(UserGroup userGroup) {
		if (userGroup.getId() == null) {
			insert(userGroup);
		} else {
			update(userGroup);
		}
	}
	
	public void insertRelation(UserGroup group, UserInGroup userInGroup) {
		dsl().insertInto(OFC_USER_USERGROUP, 
				OFC_USER_USERGROUP.GROUP_ID, 
				OFC_USER_USERGROUP.USER_ID, 
				OFC_USER_USERGROUP.REQUEST_DATE, 
				OFC_USER_USERGROUP.MEMBER_SINCE, 
				OFC_USER_USERGROUP.STATUS_CODE, 
				OFC_USER_USERGROUP.ROLE_CODE)
			.values(group.getId(), 
					userInGroup.getUserId(), 
					Daos.toTimestamp(userInGroup.getRequestDate()),
					Daos.toTimestamp(userInGroup.getMemberSince()), 
					String.valueOf(userInGroup.getJoinStatus().getCode()),
					String.valueOf(userInGroup.getRole().getCode()))
			.execute();
	}
	
	public void acceptRelation(User user, UserGroup userGroup) {
		dsl().update(OFC_USER_USERGROUP)
			.set(OFC_USER_USERGROUP.STATUS_CODE, String.valueOf(UserGroupJoinRequestStatus.ACCEPTED.getCode()))
			.where(OFC_USER_USERGROUP.GROUP_ID.eq(userGroup.getId()).and(OFC_USER_USERGROUP.USER_ID.eq(user.getId())))
			.execute();
	}
	
	public void deleteRelations(UserGroup group) {
		dsl().deleteFrom(OFC_USER_USERGROUP)
			.where(OFC_USER_USERGROUP.GROUP_ID.eq(group.getId()))
		.execute();
	}
	
	public void deleteRelation(int groupId, int userId) {
		dsl().deleteFrom(OFC_USER_USERGROUP)
			.where(OFC_USER_USERGROUP.USER_ID.eq(userId)
				.and(OFC_USER_USERGROUP.GROUP_ID.eq(groupId))
		).execute();
	}
	
	public void deleteAllUserRelations(int userId) {
		dsl().deleteFrom(OFC_USER_USERGROUP)
			.where(OFC_USER_USERGROUP.USER_ID.eq(userId))
			.execute();
	}
	
	public void updateRelation(UserInGroup userInGroup) {
		dsl().update(OFC_USER_USERGROUP)
			.set(OFC_USER_USERGROUP.MEMBER_SINCE, Daos.toTimestamp(userInGroup.getMemberSince()))
			.set(OFC_USER_USERGROUP.STATUS_CODE, String.valueOf(userInGroup.getJoinStatus().getCode()))
			.set(OFC_USER_USERGROUP.ROLE_CODE, String.valueOf(userInGroup.getRole().getCode()))
			.where(OFC_USER_USERGROUP.GROUP_ID.eq(userInGroup.getGroupId())
					.and(OFC_USER_USERGROUP.USER_ID.eq(userInGroup.getUserId()))
			)
			.execute();
	}

	public List<UserInGroup> findUsersByGroup(int userGroupId) {
		return findUsersInGroup(userGroupId, null);
	}
	
	public UserInGroup findUserInGroup(int userGroupId, int userId) {
		List<UserInGroup> usersInGroup = findUsersInGroup(userGroupId, userId);
		return usersInGroup.isEmpty() ? null : usersInGroup.get(0);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<UserInGroup> findUsersInGroup(final int userGroupId, final Integer userId) {
		final List<UserInGroup> result = new ArrayList<UserInGroup>();
		
		Condition conditions = OFC_USER_USERGROUP.GROUP_ID.eq(userGroupId);
		if (userId != null) {
			conditions = conditions.and(OFC_USER_USERGROUP.USER_ID.eq(userId));
		}
		dsl().select(OFC_USER_USERGROUP.USER_ID, 
				OFC_USER_USERGROUP.ROLE_CODE, 
				OFC_USER_USERGROUP.STATUS_CODE,
				OFC_USER_USERGROUP.REQUEST_DATE,
				OFC_USER_USERGROUP.MEMBER_SINCE,
				OFC_USER.USERNAME, 
				OFC_USER.ENABLED)
			.from(OFC_USER_USERGROUP).join(OFC_USER).on(OFC_USER.ID.eq(OFC_USER_USERGROUP.USER_ID))
			.where(conditions)
			.orderBy(OFC_USER.USERNAME)
			.fetchInto(new RecordHandler() {
				public void next(Record record) {
					UserInGroup userInGroup = new UserInGroup();
					userInGroup.setGroupId(userGroupId);
					userInGroup.setUserId(userId == null ? record.getValue(OFC_USER_USERGROUP.USER_ID) : userId);
					userInGroup.setRole(UserGroupRole.fromCode(record.getValue(OFC_USER_USERGROUP.ROLE_CODE)));
					userInGroup.setMemberSince(record.getValue(OFC_USER_USERGROUP.MEMBER_SINCE));
					userInGroup.setRequestDate(record.getValue(OFC_USER_USERGROUP.REQUEST_DATE));
					userInGroup.setJoinStatus(UserGroupJoinRequestStatus.fromCode(record.getValue(OFC_USER_USERGROUP.STATUS_CODE)));
					result.add(userInGroup);
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
						.where(OFC_USER_USERGROUP.USER_ID.eq(user.getId()))
				)
			)
			.orderBy(OFC_USERGROUP.PARENT_ID, OFC_USERGROUP.NAME)
			.fetchInto(UserGroup.class);
		return result;
	}

	public List<UserGroup> findPublicGroups() {
		return findGroups(null, Visibility.PUBLIC);
	}
	
	public List<UserGroup> findPublicUserDefinedGroups() {
		return findGroups(false, Visibility.PUBLIC);
	}
	
	public List<UserGroup> findGroups(Boolean systemDefined, Visibility visibility) {
		SelectQuery<Record> q = dsl().selectQuery();
		q.addFrom(OFC_USERGROUP);
		if (systemDefined != null) {
			q.addConditions(OFC_USERGROUP.SYSTEM_DEFINED.eq(systemDefined));
		}
		if (visibility != null) {
			q.addConditions(OFC_USERGROUP.VISIBILITY_CODE.eq(String.valueOf(Visibility.PUBLIC.getCode())));
		}
		return q.fetchInto(UserGroup.class);
	}

	
	public UserGroup loadByName(String name) {
		return dsl()
			.selectFrom(OFC_USERGROUP)
			.where(OFC_USERGROUP.NAME.eq(name))
			.fetchOneInto(UserGroup.class);
	}
	
	public List<Integer> findChildrenGroupIds(int groupId) {
		return dsl()
				.select(OFC_USERGROUP.ID)
				.from(OFC_USERGROUP)
				.where(OFC_USERGROUP.PARENT_ID.eq(groupId))
				.fetch(OFC_USERGROUP.ID);
	}

	private DSLContext dsl() {
		return DSL.using(configuration());
	}

}
