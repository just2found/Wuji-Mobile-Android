package io.weline.repo.api

import io.weline.repo.api.GroupUserPerm.PERM_MANAGE_GROUP
import io.weline.repo.api.GroupUserPerm.PERM_CREATE_GROUP
import io.weline.repo.api.GroupUserPerm.PERM_VIEW_GROUP
import junit.framework.TestCase

import org.junit.Test


/**
 * Description:
 *
 * @author admin
 * CreateDate: 2021/6/16
 */

class GroupUserPermTest : TestCase() {


    @Test
    fun testIsUploadEnable() {
        val perm1 = PERM_VIEW_GROUP
        println(perm1)
        assertFalse(GroupUserPerm.isUploadEnable(perm1))
        val perm = PERM_VIEW_GROUP or PERM_CREATE_GROUP
        println(perm)
        assertTrue(GroupUserPerm.isUploadEnable(perm))
    }

    @Test
    fun testIsManageEnable() {
        assertFalse(GroupUserPerm.isManageEnable(PERM_VIEW_GROUP))
        val perm = PERM_VIEW_GROUP or PERM_CREATE_GROUP
        println(perm)
        assertFalse(GroupUserPerm.isManageEnable(perm))
        val perm2 = PERM_VIEW_GROUP or PERM_CREATE_GROUP or PERM_MANAGE_GROUP
        println(perm2)
        assertTrue(GroupUserPerm.isManageEnable(perm2))

    }

    @Test
    fun testSwitchUploadEnable() {
        var switchUploadEnable = GroupUserPerm.switchUploadEnable(PERM_VIEW_GROUP, true)
        assertTrue(GroupUserPerm.isUploadEnable(switchUploadEnable))
        var switchUploadEnable2 = GroupUserPerm.switchUploadEnable(switchUploadEnable, false)
        assertFalse(GroupUserPerm.isUploadEnable(switchUploadEnable2))

    }

    @Test
    fun testSwitchManagementEnable() {
        var switchUploadEnable = GroupUserPerm.switchManagementEnable(PERM_VIEW_GROUP, true)
        assertTrue(GroupUserPerm.isManageEnable(switchUploadEnable))
        var switchUploadEnable2 = GroupUserPerm.switchManagementEnable(switchUploadEnable, false)
        assertFalse(GroupUserPerm.isManageEnable(switchUploadEnable2))
    }
}