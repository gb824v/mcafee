/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Girish
 * 
 */
public class DBUtils
{

	public static int getRuleGroupId(Connection connection, String name, String ruleGroupType, String platform) throws SQLException
	{

		PreparedStatement stmt = null;
		int ruleGroupId = -1;
		try
		{
			String query = "select ID from dbo.SCOR_POLICY_GROUP where GROUP_NAME = ? AND GROUP_TYPE = ? AND GROUP_PLATFORM = ?";

			stmt = connection.prepareStatement(query);
			stmt.setString(1, name);
			stmt.setString(2, ruleGroupType);
			stmt.setString(3, platform);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				ruleGroupId = rs.getInt(1);
				break;
			}
		} finally
		{
			stmt.close();
		}

		return ruleGroupId;

	}
	public static Set<Integer> getRuleGroupIDs(Connection connection, List<String> ruleGroupNames, String ruleGroupType, String platform)
			throws SQLException
	{

		PreparedStatement stmt = null;
		Set<Integer> ruleGroupIDs = new HashSet<Integer>();
		try
		{
			String query = "select ID from dbo.SCOR_POLICY_GROUP where GROUP_NAME in (#1) AND GROUP_TYPE = ? AND GROUP_PLATFORM = ?";
			query = query.replace("#1", StaticUtils.getCommanSeperated(ruleGroupNames));
			stmt = connection.prepareStatement(query);
			stmt.setString(1, ruleGroupType);
			stmt.setString(2, platform);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				ruleGroupIDs.add(rs.getInt(1));
			}
		} finally
		{
			stmt.close();
		}

		return ruleGroupIDs;

	}

	public static int getPolicyID(Connection connection, String policyName, int policyTypeID) throws SQLException
	{

		PreparedStatement stmt = null;
		int policyID = -1;
		try
		{
			String query = "select PolicyObjectID from dbo.EPOPolicyObjects where Name = ? and TypeID = ?";

			stmt = connection.prepareStatement(query);
			stmt.setString(1, policyName);
			stmt.setInt(2, policyTypeID);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				policyID = rs.getInt(1);
				break;
			}
		} finally
		{
			stmt.close();
		}

		return policyID;

	}

	public static Set<Integer> getPolicyIDs(Connection connection, List<String> policyNames, int policyTypeID) throws SQLException
	{

		PreparedStatement stmt = null;
		Set<Integer> policyIDs = new HashSet<Integer>();
		try
		{
			String query = "select PolicyObjectID from dbo.EPOPolicyObjects where Name in (#1) and TypeID = ?";
			query = query.replace("#1", StaticUtils.getCommanSeperated(policyNames));
			stmt = connection.prepareStatement(query);
			stmt.setInt(1, policyTypeID);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				policyIDs.add(rs.getInt(1));
			}
		} finally
		{
			stmt.close();
		}
		return policyIDs;

	}

	public static int getpolicyTypeID(Connection connection, String category) throws SQLException
	{

		PreparedStatement stmt = null;
		int policyTypeID = -1;
		try
		{
			String query = "select TypeID from EPOPolicyTypes where FeatureTextID = 'RSDSensorSettings' and CategoryTextID = ?";

			stmt = connection.prepareStatement(query);
			stmt.setString(1, category);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				policyTypeID = rs.getInt(1);
				break;
			}
		} finally
		{
			stmt.close();
		}

		return policyTypeID;

	}
	public static int getPolicySettingID(Connection connection, String policyName, int policyTypeID) throws SQLException
	{

		PreparedStatement stmt = null;
		Integer policySettingID = -1;
		try
		{
			String query = "select distinct PolicySettingsID from EPOPolicyObjectToSettings where PolicyObjectID = (select PolicyObjectID from dbo.EPOPolicyObjects where Name = ? and TypeID = ?)";
			stmt = connection.prepareStatement(query);
			stmt.setString(1, policyName);
			stmt.setInt(2, policyTypeID);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				policySettingID = rs.getInt(1);
			}
		} finally
		{
			stmt.close();
		}

		return policySettingID;

	}
	public static int getClientTaskTypeID(Connection connection, String typeName) throws SQLException
	{

		PreparedStatement stmt = null;
		Integer taskTypeID = -1;
		try
		{
			String query = "select TaskTypeId from dbo.EPOTaskTypes where TaskType = ? and ProductCode = 'SOLIDCORE_META'";
			stmt = connection.prepareStatement(query);
			stmt.setString(1, typeName);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				taskTypeID = rs.getInt(1);
			}
		} finally
		{
			stmt.close();
		}

		return taskTypeID;

	}
}
