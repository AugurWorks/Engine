import com.augurworks.engine.Role
import com.augurworks.engine.User
import com.augurworks.engine.UserRole

class BootStrap {

	def dataGeneratorService
	
	def createUser(name, role) {
		User me = new User(username: name).save()
		UserRole.create(me, role, true)
	}

    def init = { servletContext ->
		println 'Bootstrapping'
		def adminRole = new Role(authority: "ROLE_ADMIN").save()
		def userRole = new Role(authority: "ROLE_USER").save()
		createUser('TheConnMan', adminRole);

		dataGeneratorService.importQuandlDataSets();
    }

    def destroy = {
		
    }
}
