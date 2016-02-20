/*
 * Copyright 2007 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dao.hibernate;

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Implemtation of UserDao using Hibernate persistence objects.
 */
public class UserDaoImpl extends AbstractDaoImpl implements UserDao {

    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user is required");
        }

        if (user.getId() != -1) {
            throw new IllegalArgumentException("new user is required");
        }

        if (findUserByEmailIgnoreCase(user.getEmail()) != null) {
            throw new DuplicateEmailException(user.getEmail());
        }

        getSession().save(user);
        getSession().flush();
        return user;
    }

    public User getUser(String email) {
        return findUserByEmail(email);
    }

    public User getUserByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email required");
        }
        return findUserByEmail(email);
    }

    public Set<User> getUsers() {
        Set<User> users = new HashSet<>();
        Iterator it = getSession().getNamedQuery("user.all").iterate();
        while (it.hasNext()) {
            users.add((User) it.next());
        }

        return users;
    }

    public void removeUser(String email) {
        User user = findUserByEmail(email);
        // delete user
        if (user != null) {
            removeUser(user);
        }
    }

    public void removeUser(User user) {
        getSession().delete(user);
        getSession().flush();
    }

    public User updateUser(User user) {
        // prevent auto flushing when querying for existing users
        getSession().setFlushMode(FlushMode.MANUAL);

        User findUser = findUserByEmailIgnoreCaseAndId(user.getId(), user.getEmail());

        if (findUser != null) {
            if (findUser.getEmail().equals(user.getEmail())) {
                throw new DuplicateEmailException(user.getEmail());
            }
        }

        getSession().update(user);
        getSession().flush();

        return user;
    }

    private User findUserByEmailIgnoreCaseAndId(Long userId, String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery(
                "user.byUsernameOrEmail.ignorecase.ingoreId")
                .setParameter("email", email)
                .setParameter("userid", userId);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }

    private User findUserByEmail(String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byEmail").setParameter(
                "email", email);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }

    private User findUserByEmailIgnoreCase(String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byEmail.ignorecase").setParameter(
                "email", email);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }
}
