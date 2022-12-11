package com.isabel.aimCrafter.db

import com.isabel.aimCrafter.db.model.UserAndCraftsDb
import com.isabel.aimCrafter.db.model.UserDb
import com.isabel.aimCrafter.db.model.UserInfoDb
import com.isabel.aimCrafter.db.model.UsernameDb
import com.isabel.aimCrafter.rest.model.SimplifiedCraftResponse
import com.isabel.aimCrafter.rest.model.UserSignIn
import com.isabel.aimCrafter.rest.model.UserSignUp
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class UserDao(
    val jdbcTemplate: NamedParameterJdbcTemplate
) {
    fun signUp(user: UserSignUp): UserDb {
        return jdbcTemplate.queryForObject(
            """
                INSERT INTO users (firstName, lastName, username, email, passwordDigest)
                VALUES (:firstName, :lastName, :username, :email, :passwordDigest)
                RETURNING id, username, firstName, lastName, email, passwordDigest, createdAt, updatedAt
            """,
            mapOf(
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "username" to user.username,
                "email" to user.email,
                "passwordDigest" to user.password
            )
        ) { rs, _ ->
            UserDb(
                id = rs.getLong("id"),
                username = rs.getString("username"),
                firstName = rs.getString("firstName"),
                lastName = rs.getString("lastName"),
                email = rs.getString("email"),
                passwordDigest = rs.getString("passwordDigest"),
                createdAt = rs.getTimestamp("createdAt"),
                updatedAt = rs.getTimestamp("updatedAt")
            )
        }!!
    }

    fun signIn(user: UserSignIn): UserDb? {
        return jdbcTemplate.query(
            """
                SELECT id, firstName, lastName, username, email, passwordDigest, createdAt, updatedAt
                FROM users
                WHERE email = :email AND passwordDigest = :passwordDigest
            """,
            mapOf(
                "email" to user.email,
                "passwordDigest" to user.password,
            )
        ) { rs, _ ->
            UserDb(
                id = rs.getLong("id"),
                username = rs.getString("username"),
                firstName = rs.getString("firstName"),
                lastName = rs.getString("lastName"),
                email = rs.getString("email"),
                passwordDigest = rs.getString("passwordDigest"),
                createdAt = rs.getTimestamp("createdAt"),
                updatedAt = rs.getTimestamp("updatedAt")
            )
        }.singleOrNull()
    }

    fun showUser(username: String): UserAndCraftsDb? {
        val user = jdbcTemplate.query(
            """
                    SELECT userName, id
                    FROM users
                    WHERE username = :username
                """,
            mapOf("username" to username)
        ) { rs, _ ->
            UserInfoDb(
                id = rs.getLong("id"),
                username = rs.getString("username")
            )
        }.singleOrNull() ?: return null
        val crafts = jdbcTemplate.query(
            """
                SELECT title, image, createdAt
                FROM crafts
                WHERE userId = :userId 
            """,
            mapOf("userId" to user.id)
        ) { rs, _ ->
            SimplifiedCraftResponse(
                title = rs.getString("title"),
                image = rs.getString("image"),
                createdAt = rs.getTimestamp("createdAt").toInstant()
            )
        }
        return UserAndCraftsDb(
            username = user.username,
            crafts = crafts
        )
    }

    fun getUsername(id: Long): UsernameDb? {
        return jdbcTemplate.query(
            """
               SELECT username FROM users WHERE id = :id 
            """,
            mapOf("id" to id)
        ) { rs, _ ->
            UsernameDb(
                username = rs.getString("username")
            )
        }.singleOrNull()
    }
}