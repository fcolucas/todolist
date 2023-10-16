package br.com.fcolucasdev.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.fcolucasdev.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var serveletPath = request.getServletPath();
    if (serveletPath.startsWith("/tasks")) {
      // Pegar na autorizacao o usuario e senha
      var authorization = request.getHeader("Authorization");
      var authEncoded = authorization.substring("Basic".length()).trim();
      var authDecoded = new String(Base64.getDecoder().decode(authEncoded));
      String[] credentials = authDecoded.split(":");
      var username = credentials[0];
      var password = credentials[1];

      // Verificar se o usuario e senha estao corretos
      var user = this.userRepository.findByUsername(username);
      if (user == null) {
        response.sendError(401);
      } else {
        var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword().toCharArray());
        if (!passwordVerify.verified) {
          response.sendError(401);
        } else {
          request.setAttribute("idUser", user.getId());
          filterChain.doFilter(request, response);
        }
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }
}
