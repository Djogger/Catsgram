package ru.yandex.practicum.catsgram.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.service.PostService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public Collection<Post> findAll(@RequestParam(defaultValue = "10") int size,
                                    @RequestParam(defaultValue = "desc") String sort,
                                    @RequestParam(defaultValue = "0") int page) {
        if (size <= 0) {
            throw new ParameterNotValidException("size", "Некорректный размер выборки. Размер должен быть больше нуля.");
        }
        if (!sort.equals("asc") && !sort.equals("desc")) {
            throw new ParameterNotValidException("sort", "Указан непонятный тип сортировки.");
        }
        if (page < 0) {
            throw new ParameterNotValidException("page", "Номер страницы не может быть меньше нуля.");
        }

        return postService.findAll(size, sort, page);
    }

    @GetMapping("/post/{id}")
    public Post findPost(@PathVariable("id") Long postId) {
        Optional<Post> response = postService.findPostById(postId);

        if (response.isPresent()) {
            return response.get();
        }

        throw new ConditionsNotMetException("Пост с id: " + postId + " не найден");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    @PutMapping
    public Post update(@RequestBody Post newPost) {
        return postService.update(newPost);
    }

}