package ru.yandex.practicum.catsgram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final UserService userService;

    private final Map<Long, Post> posts = new HashMap<>();

    @Autowired
    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(int size, String sort, int from) {
        if (sort.equals("asc")) {
            return posts.values().stream()
                    .sorted(Comparator.comparing(Post::getPostDate))
                    .skip(from)
                    .limit(size)
                    .collect(Collectors.toList());
        } else if (sort.equals("desc")) {
            return posts.values().stream()
                    .sorted(Comparator.comparing(Post::getPostDate).reversed())
                    .skip(from)
                    .limit(size)
                    .collect(Collectors.toList());
        }

        throw new ConditionsNotMetException("Переданы неправильные параметры запроса");
    }

    public Post create(Post post) {
        // проверяем выполнение необходимых условий
        if (userService.findUserById(post.getAuthorId()).isEmpty()) {
            throw new ConditionsNotMetException("Автор с id: " + post.getAuthorId() + " не найден");
        }
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        // формируем дополнительные данные
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        // сохраняем новую публикацию в памяти приложения
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        // проверяем необходимые условия
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId()) && posts.get(newPost.getId()).getAuthorId() == newPost.getAuthorId()) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            // если публикация найдена и все условия соблюдены, обновляем её содержимое
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " и автором с id = " + newPost.getAuthorId() + " не найден");
    }

    public Optional<Post> findPostById(Long postId) {
        return Optional.ofNullable(posts.get(postId));
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
