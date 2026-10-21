package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void soloPermiteArchivosDeAvatarIncluidosEnLaLista() {
        assertTrue(usuarioService.fotoPerfilPermitida("Gato uno.png"));
        assertTrue(usuarioService.fotoPerfilPermitida("Gato nueve.png"));
        assertFalse(usuarioService.fotoPerfilPermitida("../Logo.png"));
        assertFalse(usuarioService.fotoPerfilPermitida("avatar-externo.png"));
        verifyNoInteractions(usuarioRepository);
    }
}
