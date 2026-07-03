<template>
  <div class="identity-access-center">
    <section class="center-hero">
      <div>
        <p class="hero-kicker">IDENTITY & ACCESS</p>
        <h2>统一管理账号、角色、菜单和接口权限</h2>
        <p class="hero-copy">
          当前版本已经接入平台账号、角色、角色授权和用户角色绑定，菜单与接口权限按后端角色授权结果生效。
        </p>
      </div>
      <div class="hero-tags">
        <span>账号治理</span>
        <span>角色治理</span>
        <span>权限授权</span>
      </div>
    </section>

    <section class="lane-grid">
      <article v-for="item in summaryCards" :key="item.label" class="lane-card implemented">
        <p class="lane-title">{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
        <span>{{ item.desc }}</span>
      </article>
    </section>

    <section class="board">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账号管理" name="users">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input
                v-model.trim="userFilters.keyword"
                class="filter-keyword"
                size="small"
                clearable
                placeholder="账号 / 姓名"
                prefix-icon="el-icon-search"
                @keyup.enter.native="loadUsers"
                @clear="loadUsers"
              />
              <el-select
                v-model="userFilters.tenantId"
                class="filter-select"
                size="small"
                clearable
                placeholder="租户"
              >
                <el-option
                  v-for="item in tenantOptions"
                  :key="item.tenantId"
                  :label="item.tenantName"
                  :value="item.tenantId"
                />
              </el-select>
              <el-select
                v-model="userFilters.status"
                class="filter-select filter-status"
                size="small"
                clearable
                placeholder="状态"
              >
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button
                class="toolbar-query-btn"
                size="small"
                type="primary"
                icon="el-icon-search"
                @click="loadUsers"
              >
                查询
              </el-button>
              <el-button class="toolbar-reset-btn" size="small" @click="resetUserFilters">重置</el-button>
            </div>
            <div class="toolbar-actions">
              <el-button class="toolbar-action-btn" size="small" icon="el-icon-refresh-right" @click="loadUsers">
                刷新
              </el-button>
              <el-button
                class="toolbar-create-btn"
                size="small"
                type="primary"
                icon="el-icon-plus"
                @click="openUserDialog()"
              >
                新增账号
              </el-button>
            </div>
          </div>

          <el-table
            v-loading="userLoading"
            :data="users"
            border
            stripe
            size="small"
            height="calc(100vh - 500px)"
          >
            <el-table-column prop="username" label="账号" min-width="120" />
            <el-table-column prop="displayName" label="姓名" min-width="120" />
            <el-table-column prop="tenantName" label="租户" min-width="160" />
            <el-table-column prop="userType" label="用户类型" min-width="100" />
            <el-table-column prop="aggregatorId" label="聚合商ID" min-width="150" show-overflow-tooltip />
            <el-table-column prop="entId" label="企业ID" min-width="150" show-overflow-tooltip />
            <el-table-column label="角色" min-width="220">
              <template slot-scope="scope">
                {{ (scope.row.roleNames || []).join("、") || "-" }}
              </template>
            </el-table-column>
            <el-table-column prop="lastLoginTime" label="最近登录" min-width="160" />
            <el-table-column label="状态" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
                  {{ scope.row.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="mini" @click="openUserDialog(scope.row)">编辑</el-button>
                <el-button type="text" size="mini" @click="openUserRoleDialog(scope.row)">角色</el-button>
                <el-button
                  type="text"
                  size="mini"
                  :loading="userStatusLoadingId === scope.row.userId"
                  @click="toggleUserStatus(scope.row)"
                >
                  {{ scope.row.status === 1 ? "停用" : "启用" }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="userPagination.total"
              :current-page.sync="userPagination.pageIndex"
              :page-size.sync="userPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              @current-change="loadUsers"
              @size-change="handleUserPageSizeChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="角色权限" name="roles">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input
                v-model.trim="roleFilters.keyword"
                class="filter-keyword"
                size="small"
                clearable
                placeholder="角色名称 / 角色编码"
                prefix-icon="el-icon-search"
                @keyup.enter.native="loadRoles"
                @clear="loadRoles"
              />
              <el-select
                v-model="roleFilters.platformType"
                class="filter-select"
                size="small"
                clearable
                placeholder="平台类型"
              >
                <el-option label="平台运营" value="owner" />
                <el-option label="客户侧" value="customer" />
              </el-select>
              <el-select
                v-model="roleFilters.status"
                class="filter-select filter-status"
                size="small"
                clearable
                placeholder="状态"
              >
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button
                class="toolbar-query-btn"
                size="small"
                type="primary"
                icon="el-icon-search"
                @click="loadRoles"
              >
                查询
              </el-button>
              <el-button class="toolbar-reset-btn" size="small" @click="resetRoleFilters">重置</el-button>
            </div>
            <div class="toolbar-actions">
              <el-button class="toolbar-action-btn" size="small" icon="el-icon-refresh-right" @click="loadRoles">
                刷新
              </el-button>
              <el-button
                class="toolbar-create-btn"
                size="small"
                type="primary"
                icon="el-icon-plus"
                @click="openRoleDialog()"
              >
                新增角色
              </el-button>
            </div>
          </div>

          <el-table
            v-loading="roleLoading"
            :data="roles"
            border
            stripe
            size="small"
            height="calc(100vh - 500px)"
          >
            <el-table-column prop="roleName" label="角色名称" min-width="140" />
            <el-table-column prop="roleCode" label="角色编码" min-width="140" />
            <el-table-column prop="platformType" label="平台类型" min-width="100" />
            <el-table-column label="状态" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
                  {{ scope.row.status === 1 ? "启用" : "停用" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
            <el-table-column prop="updateTime" label="更新时间" min-width="160" />
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="mini" @click="openRoleDialog(scope.row)">编辑</el-button>
                <el-button type="text" size="mini" @click="openPermissionDialog(scope.row)">授权</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :total="rolePagination.total"
              :current-page.sync="rolePagination.pageIndex"
              :page-size.sync="rolePagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              @current-change="loadRoles"
              @size-change="handleRolePageSizeChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog
      :title="editingUserId ? '编辑账号' : '新增账号'"
      :visible.sync="userDialogVisible"
      width="720px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="userForm"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
        size="small"
      >
        <div class="form-grid">
          <el-form-item label="账号" prop="username">
            <el-input v-model.trim="userForm.username" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model.trim="userForm.displayName" />
          </el-form-item>
          <el-form-item label="租户" prop="tenantId">
            <el-select v-model="userForm.tenantId" filterable placeholder="请选择租户" @change="handleTenantChange">
              <el-option
                v-for="item in tenantOptions"
                :key="item.tenantId"
                :label="item.tenantName"
                :value="item.tenantId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="用户类型">
            <el-select v-model="userForm.userType" placeholder="请选择">
              <el-option label="ADMIN" value="ADMIN" />
              <el-option label="CUSTOMER" value="CUSTOMER" />
            </el-select>
          </el-form-item>
          <el-form-item label="聚合商ID">
            <el-input v-model.trim="userForm.aggregatorId" />
          </el-form-item>
          <el-form-item label="企业ID">
            <el-input v-model.trim="userForm.entId" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="userForm.status" placeholder="请选择">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="userDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="userSaving" @click="saveUser">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="分配用户角色"
      :visible.sync="userRoleDialogVisible"
      width="560px"
      :close-on-click-modal="false"
    >
      <div class="role-assign">
        <p class="assign-title">{{ currentRoleUser.displayName || currentRoleUser.username || "-" }}</p>
        <el-checkbox-group v-model="selectedUserRoleIds" class="role-checks">
          <el-checkbox
            v-for="item in userRoleOptions"
            :key="item.roleId"
            :label="item.roleId"
          >
            {{ item.roleName }} / {{ item.roleCode }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <span slot="footer">
        <el-button size="small" @click="userRoleDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="userRoleSaving" @click="saveUserRoles">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog
      :title="editingRoleId ? '编辑角色' : '新增角色'"
      :visible.sync="roleDialogVisible"
      width="680px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="roleForm"
        :model="roleForm"
        :rules="roleRules"
        label-width="100px"
        size="small"
      >
        <div class="form-grid">
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model.trim="roleForm.roleName" />
          </el-form-item>
          <el-form-item label="角色编码" prop="roleCode">
            <el-input v-model.trim="roleForm.roleCode" />
          </el-form-item>
          <el-form-item label="平台类型" prop="platformType">
            <el-select v-model="roleForm.platformType" placeholder="请选择">
              <el-option label="平台运营" value="owner" />
              <el-option label="客户侧" value="customer" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="roleForm.status" placeholder="请选择">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" class="full">
            <el-input v-model.trim="roleForm.remark" type="textarea" :rows="3" />
          </el-form-item>
        </div>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="roleDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="roleSaving" @click="saveRole">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="角色权限授权"
      :visible.sync="permissionDialogVisible"
      width="760px"
      :close-on-click-modal="false"
    >
      <div v-loading="permissionLoading" class="permission-panel">
        <div class="permission-head">
          <p>{{ currentPermissionRole.roleName || "-" }}</p>
          <span>{{ currentPermissionRole.platformType || "-" }}</span>
        </div>
        <el-tree
          v-if="permissionTree.length"
          ref="permissionTree"
          :data="permissionTree"
          node-key="permissionCode"
          show-checkbox
          default-expand-all
          :props="treeProps"
          :default-checked-keys="checkedPermissionKeys"
        />
        <div v-else class="empty-state">暂无可授权权限</div>
      </div>
      <span slot="footer">
        <el-button size="small" @click="permissionDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="permissionSaving" @click="savePermissions">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  createConsoleUser,
  createRole,
  fetchConsoleUserPage,
  fetchPermissionTree,
  fetchRolePage,
  fetchTenantPage,
  saveConsoleUserRoles,
  saveRolePermissions,
  updateConsoleUser,
  updateConsoleUserStatus,
  updateRole,
} from "@/modules/platform/api"

const EMPTY_USER_FORM = () => ({
  username: "",
  displayName: "",
  tenantId: "",
  userType: "CUSTOMER",
  aggregatorId: "",
  entId: "",
  status: 1,
})

const EMPTY_ROLE_FORM = () => ({
  roleName: "",
  roleCode: "",
  platformType: "owner",
  status: 1,
  remark: "",
})

export default {
  name: "IdentityAccessCenter",
  data() {
    return {
      activeTab: "users",
      userLoading: false,
      roleLoading: false,
      userSaving: false,
      roleSaving: false,
      userRoleSaving: false,
      permissionLoading: false,
      permissionSaving: false,
      userStatusLoadingId: "",
      userDialogVisible: false,
      roleDialogVisible: false,
      userRoleDialogVisible: false,
      permissionDialogVisible: false,
      editingUserId: "",
      editingRoleId: "",
      users: [],
      roles: [],
      tenantOptions: [],
      userRoleOptions: [],
      permissionTree: [],
      checkedPermissionKeys: [],
      selectedUserRoleIds: [],
      userFilters: {
        keyword: "",
        tenantId: "",
        status: null,
      },
      roleFilters: {
        keyword: "",
        platformType: "",
        status: null,
      },
      userPagination: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      rolePagination: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
      },
      userForm: EMPTY_USER_FORM(),
      roleForm: EMPTY_ROLE_FORM(),
      currentRoleUser: {},
      currentPermissionRole: {},
      treeProps: {
        children: "children",
        label: "permissionName",
      },
      userRules: {
        username: [{ required: true, message: "请输入账号", trigger: "blur" }],
        tenantId: [{ required: true, message: "请选择租户", trigger: "change" }],
      },
      roleRules: {
        roleName: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
        roleCode: [{ required: true, message: "请输入角色编码", trigger: "blur" }],
        platformType: [{ required: true, message: "请选择平台类型", trigger: "change" }],
      },
    }
  },
  computed: {
    activeUserCount() {
      return this.users.filter(item => item.status === 1).length
    },
    roleBoundUserCount() {
      return this.users.filter(item => (item.roleIds || []).length > 0).length
    },
    activeRoleCount() {
      return this.roles.filter(item => item.status === 1).length
    },
    summaryCards() {
      return [
        {
          label: "平台账号",
          value: String(this.userPagination.total || 0),
          desc: `当前页启用 ${this.activeUserCount} 个账号`,
        },
        {
          label: "角色数量",
          value: String(this.rolePagination.total || 0),
          desc: `当前页启用 ${this.activeRoleCount} 个角色`,
        },
        {
          label: "已绑定角色账号",
          value: String(this.roleBoundUserCount),
          desc: "当前页至少绑定一个角色的账号数量",
        },
        {
          label: "租户范围",
          value: String(this.tenantOptions.length),
          desc: "可绑定到账号的租户主体数量",
        },
      ]
    },
  },
  mounted() {
    this.loadBaseOptions()
    this.loadUsers()
    this.loadRoles()
  },
  methods: {
    loadBaseOptions() {
      fetchTenantPage({ pageIndex: 1, pageSize: 200 })
        .then(data => {
          this.tenantOptions = Array.isArray(data.list) ? data.list : []
        })
        .catch(error => {
          this.$message.error(error.message || "租户选项加载失败")
        })
    },
    loadUsers() {
      this.userLoading = true
      fetchConsoleUserPage({
        ...this.userFilters,
        pageIndex: this.userPagination.pageIndex,
        pageSize: this.userPagination.pageSize,
      })
        .then(data => {
          this.users = Array.isArray(data.list) ? data.list : []
          this.userPagination.total = data.total || 0
          this.userPagination.pageIndex = data.pageIndex || this.userPagination.pageIndex
          this.userPagination.pageSize = data.pageSize || this.userPagination.pageSize
        })
        .catch(error => {
          this.$message.error(error.message || "账号列表加载失败")
        })
        .finally(() => {
          this.userLoading = false
        })
    },
    loadRoles() {
      this.roleLoading = true
      fetchRolePage({
        ...this.roleFilters,
        pageIndex: this.rolePagination.pageIndex,
        pageSize: this.rolePagination.pageSize,
      })
        .then(data => {
          this.roles = Array.isArray(data.list) ? data.list : []
          this.rolePagination.total = data.total || 0
          this.rolePagination.pageIndex = data.pageIndex || this.rolePagination.pageIndex
          this.rolePagination.pageSize = data.pageSize || this.rolePagination.pageSize
        })
        .catch(error => {
          this.$message.error(error.message || "角色列表加载失败")
        })
        .finally(() => {
          this.roleLoading = false
        })
    },
    resetUserFilters() {
      this.userFilters = {
        keyword: "",
        tenantId: "",
        status: null,
      }
      this.userPagination.pageIndex = 1
      this.loadUsers()
    },
    resetRoleFilters() {
      this.roleFilters = {
        keyword: "",
        platformType: "",
        status: null,
      }
      this.rolePagination.pageIndex = 1
      this.loadRoles()
    },
    handleUserPageSizeChange() {
      this.userPagination.pageIndex = 1
      this.loadUsers()
    },
    handleRolePageSizeChange() {
      this.rolePagination.pageIndex = 1
      this.loadRoles()
    },
    openUserDialog(row) {
      this.editingUserId = row && row.userId ? row.userId : ""
      this.userForm = row
        ? {
          username: row.username || "",
          displayName: row.displayName || "",
          tenantId: row.tenantId || "",
          userType: row.userType || "CUSTOMER",
          aggregatorId: row.aggregatorId || "",
          entId: row.entId || "",
          status: row.status == null ? 1 : row.status,
        }
        : EMPTY_USER_FORM()
      this.userDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.userForm) {
          this.$refs.userForm.clearValidate()
        }
      })
    },
    handleTenantChange(tenantId) {
      const tenant = this.tenantOptions.find(item => item.tenantId === tenantId)
      if (!tenant) {
        return
      }
      if (!this.userForm.aggregatorId) {
        this.userForm.aggregatorId = tenant.aggregatorId || ""
      }
      if (!this.userForm.entId) {
        this.userForm.entId = tenant.entId || ""
      }
      if (tenant.tenantType === "PLATFORM") {
        this.userForm.userType = "ADMIN"
      }
    },
    saveUser() {
      this.$refs.userForm.validate(valid => {
        if (!valid) {
          return
        }
        this.userSaving = true
        const payload = { ...this.userForm }
        const request = this.editingUserId
          ? updateConsoleUser(this.editingUserId, payload)
          : createConsoleUser(payload)
        request
          .then(() => {
            this.$message.success("账号保存成功")
            this.userDialogVisible = false
            this.loadUsers()
            this.loadBaseOptions()
          })
          .catch(error => {
            this.$message.error(error.message || "账号保存失败")
          })
          .finally(() => {
            this.userSaving = false
          })
      })
    },
    toggleUserStatus(row) {
      const status = row.status === 1 ? 0 : 1
      this.userStatusLoadingId = row.userId
      updateConsoleUserStatus(row.userId, status)
        .then(() => {
          this.$message.success(status === 1 ? "账号已启用" : "账号已停用")
          this.loadUsers()
        })
        .catch(error => {
          this.$message.error(error.message || "账号状态更新失败")
        })
        .finally(() => {
          this.userStatusLoadingId = ""
        })
    },
    openUserRoleDialog(row) {
      this.currentRoleUser = row || {}
      this.selectedUserRoleIds = Array.isArray(row.roleIds) ? row.roleIds.slice() : []
      const platformType = this.inferUserPlatformType(row)
      fetchRolePage({
        pageIndex: 1,
        pageSize: 200,
        platformType,
        status: 1,
      })
        .then(data => {
          this.userRoleOptions = Array.isArray(data.list) ? data.list : []
          this.userRoleDialogVisible = true
        })
        .catch(error => {
          this.$message.error(error.message || "角色选项加载失败")
        })
    },
    saveUserRoles() {
      this.userRoleSaving = true
      saveConsoleUserRoles(this.currentRoleUser.userId, this.selectedUserRoleIds)
        .then(() => {
          this.$message.success("用户角色保存成功")
          this.userRoleDialogVisible = false
          this.loadUsers()
        })
        .catch(error => {
          this.$message.error(error.message || "用户角色保存失败")
        })
        .finally(() => {
          this.userRoleSaving = false
        })
    },
    inferUserPlatformType(row) {
      return String(row && row.userType ? row.userType : "").toUpperCase() === "ADMIN" ? "owner" : "customer"
    },
    openRoleDialog(row) {
      this.editingRoleId = row && row.roleId ? row.roleId : ""
      this.roleForm = row
        ? {
          roleName: row.roleName || "",
          roleCode: row.roleCode || "",
          platformType: row.platformType || "owner",
          status: row.status == null ? 1 : row.status,
          remark: row.remark || "",
        }
        : EMPTY_ROLE_FORM()
      this.roleDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.roleForm) {
          this.$refs.roleForm.clearValidate()
        }
      })
    },
    saveRole() {
      this.$refs.roleForm.validate(valid => {
        if (!valid) {
          return
        }
        this.roleSaving = true
        const request = this.editingRoleId
          ? updateRole(this.editingRoleId, this.roleForm)
          : createRole(this.roleForm)
        request
          .then(() => {
            this.$message.success("角色保存成功")
            this.roleDialogVisible = false
            this.loadRoles()
          })
          .catch(error => {
            this.$message.error(error.message || "角色保存失败")
          })
          .finally(() => {
            this.roleSaving = false
          })
      })
    },
    openPermissionDialog(row) {
      this.currentPermissionRole = row || {}
      this.permissionDialogVisible = true
      this.permissionLoading = true
      this.permissionTree = []
      this.checkedPermissionKeys = []
      fetchPermissionTree({
        platformType: row.platformType,
        roleId: row.roleId,
      })
        .then(data => {
          const tree = Array.isArray(data) ? data : []
          this.permissionTree = tree
          this.checkedPermissionKeys = this.collectCheckedKeys(tree)
          this.$nextTick(() => {
            if (this.$refs.permissionTree) {
              this.$refs.permissionTree.setCheckedKeys(this.checkedPermissionKeys)
            }
          })
        })
        .catch(error => {
          this.$message.error(error.message || "权限树加载失败")
          this.permissionDialogVisible = false
        })
        .finally(() => {
          this.permissionLoading = false
        })
    },
    collectCheckedKeys(nodes) {
      const result = []
      ;(nodes || []).forEach(node => {
        if (node.checked && !String(node.permissionCode || "").startsWith("module:")) {
          result.push(node.permissionCode)
        }
        result.push(...this.collectCheckedKeys(node.children || []))
      })
      return result
    },
    savePermissions() {
      const checkedKeys = this.$refs.permissionTree
        ? this.$refs.permissionTree.getCheckedKeys().filter(key => !String(key).startsWith("module:"))
        : []
      this.permissionSaving = true
      saveRolePermissions(this.currentPermissionRole.roleId, checkedKeys)
        .then(() => {
          this.$message.success("角色权限保存成功")
          this.permissionDialogVisible = false
        })
        .catch(error => {
          this.$message.error(error.message || "角色权限保存失败")
        })
        .finally(() => {
          this.permissionSaving = false
        })
    },
  },
}
</script>

<style lang="less" scoped>
.identity-access-center {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.center-hero {
  min-height: 168px;
  padding: 24px;
  border-radius: 10px;
  background:
    linear-gradient(135deg, rgba(11, 36, 71, 0.96), rgba(17, 94, 89, 0.92)),
    #0e2638;
  color: #ffffff;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.hero-kicker,
.hero-copy,
.center-hero h2,
.hero-tags span,
.lane-title,
.lane-card strong,
.lane-card span,
.assign-title,
.permission-head p,
.permission-head span {
  margin: 0;
}

.hero-kicker {
  color: #9bf6ea;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.center-hero h2 {
  margin-top: 10px;
  max-width: 680px;
  font-size: 30px;
  line-height: 1.25;
}

.hero-copy {
  margin-top: 12px;
  max-width: 760px;
  color: #d7e6ef;
  font-size: 14px;
  line-height: 1.7;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.hero-tags span {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.16);
  font-size: 12px;
}

.lane-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.lane-card,
.board {
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #dde6ed;
}

.lane-card {
  min-height: 122px;
  padding: 18px;
}

.lane-title {
  color: #607d8f;
  font-size: 13px;
}

.lane-card strong {
  display: block;
  margin-top: 12px;
  color: #0e2638;
  font-size: 24px;
  font-weight: 700;
}

.lane-card span {
  display: block;
  margin-top: 10px;
  color: #607d8f;
  font-size: 13px;
  line-height: 1.6;
}

.lane-card.implemented {
  background: linear-gradient(180deg, #ffffff, #f7fbff);
}

.board {
  padding: 16px;
}

.toolbar {
  margin-bottom: 14px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.toolbar-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-filters /deep/ .filter-keyword {
  width: 300px;
}

.toolbar-filters /deep/ .filter-select {
  width: 180px;
}

.toolbar-filters /deep/ .filter-status {
  width: 140px;
}

.toolbar-query-btn,
.toolbar-reset-btn,
.toolbar-action-btn {
  min-width: 88px;
}

.toolbar-create-btn {
  min-width: 112px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.role-assign {
  min-height: 180px;
}

.assign-title {
  color: #0e2638;
  font-size: 18px;
  font-weight: 700;
}

.role-checks {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.permission-panel {
  min-height: 320px;
}

.permission-head {
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.permission-head p {
  color: #0e2638;
  font-size: 18px;
  font-weight: 700;
}

.permission-head span {
  color: #607d8f;
  font-size: 12px;
}

.empty-state {
  padding: 48px 16px;
  text-align: center;
  color: #8a9dac;
}

@media (max-width: 1280px) {
  .lane-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-actions {
    justify-content: flex-end;
  }
}

@media (max-width: 960px) {
  .center-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-tags {
    justify-content: flex-start;
  }

  .lane-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-filters /deep/ .filter-keyword,
  .toolbar-filters /deep/ .filter-select,
  .toolbar-filters /deep/ .filter-status {
    width: 100%;
  }
}
</style>
